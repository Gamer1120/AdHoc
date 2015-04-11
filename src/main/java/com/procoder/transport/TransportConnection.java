package com.procoder.transport;

import com.procoder.AdhocApplication;
import com.procoder.AdhocNetwork;
import com.procoder.NetworkLayer;
import com.procoder.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TransportConnection {

    private static final ScheduledThreadPoolExecutor TIMEOUT_EXECUTOR = new ScheduledThreadPoolExecutor(2);
    private static final long ACK_TIMEOUT = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportConnection.class);

// ------------------ Instance variables ----------------


    private InetAddress receivingHost;
    private HashMap<Long, ScheduledFuture> unAckedSegmentTasks;
    private AdhocApplication adhocApplication;
    private Queue<Byte> sendQueue;
    private SortedMap<Integer, TransportSegment> receivedSegments;
    private AdhocNetwork networkLayer;
    private int seq; // Huidige sequence nummer verzendende kant
    private int nextAck; // Huidige sequence van de ontvangende kant
    private boolean synSent;
    private boolean synReceived;
    private boolean established;


// --------------------- Constructors -------------------

    public TransportConnection(InetAddress host, AdhocNetwork networkLayer, AdhocApplication app) {
        receivingHost = host;
        unAckedSegmentTasks = new HashMap<>();
        sendQueue = new LinkedList<>();
        receivedSegments = new TreeMap<>();
        this.networkLayer = networkLayer;
        seq = new Random().nextInt();
        synSent = false;
        synReceived = false;
        adhocApplication = app;
    }

// ----------------------- Queries ----------------------

// ----------------------- Commands ---------------------

    public void sendSyn() {
        TransportSegment syn = new TransportSegment(new Byte[0], seq);
        // Het Syn pakket gebruikt 1 data byte zodat het geackt kan worden.
        seq++;
        syn.setSyn();

        String debug = "[TL] [SND]: Ik stuur nu een SYN";

        // Als de andere kant al een Syn heeft gestuurd dan
        if (synReceived) {
            syn.setAck(nextAck);
            debug = "[TL] [SND]: Ik stuur nu een SYN ACK";
            LOGGER.debug(debug + "voor de eerste keer");
        }

        final String finalDebug = debug;

        ScheduledFuture retransmitTask = TIMEOUT_EXECUTOR.scheduleAtFixedRate(() -> {
            networkLayer.send(receivingHost, syn.toByteArray());
            LOGGER.debug(finalDebug);

        }, 0, 1000, TimeUnit.MILLISECONDS);

        unAckedSegmentTasks.put((long) (syn.seq), retransmitTask);
        synSent = true;

    }

    public void sendByte(byte b) {
        sendQueue.add(b);
    }

    public void processSendQueue() {


        if (!synSent) {
            sendSyn();
        } else if(established) {

            List<Byte> data = new LinkedList<>();

            Iterator<Byte> it = sendQueue.iterator();

            while (it.hasNext()) {

                while (it.hasNext() && data.size() < 1400) {
                    // Add byte to data to be sent
                    data.add(it.next());
                    // This data will be sent, so it can be removed from the queue
                    it.remove();
                }
                TransportSegment segment = new TransportSegment(data.toArray(new Byte[data.size()]), seq);
                if (synReceived) {
                    segment.setAck(nextAck);
                }
                seq += data.size();
                LOGGER.debug("[TL] [SND]: {}", segment.data.length);

                // Segment is nog niet geacked dus toevoegen aan de ongeackte segments en schedule de retransmit.

                ScheduledFuture retransmitTask = TIMEOUT_EXECUTOR.scheduleAtFixedRate(() -> {
                    networkLayer.send(receivingHost, segment.toByteArray());

                }, 0, 1000, TimeUnit.MILLISECONDS);

                unAckedSegmentTasks.put((long) (segment.seq + segment.data.length - 1), retransmitTask);
                data.clear();

                LOGGER.debug("[TL] [SND] Sending segment  seq: " + segment.seq + " ack: " + segment.ack + " Syn: " + segment.isSyn() + " data: " + segment.data.length);

            }

        }


    }

    public void removeAckedSegment(TransportSegment segment) {

        // Natuurlijk alleen doen als de ack geldig is
        if (segment.validAck()) {
            // Verwijder alle niet geackte segments met een seq + length
            Iterator<Map.Entry<Long, ScheduledFuture>> it = unAckedSegmentTasks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, ScheduledFuture> entry = it.next();
                // FIXME Hier zit volgens mij nog een edge case in bij sequence number wrapping
                if (entry.getKey() < segment.ack) {
                    entry.getValue().cancel(false);
                    it.remove();
                }
            }
        }




    }

    public void receiveData(TransportSegment segment) {

        LOGGER.debug("[TL] [RCV] Processing segment  seq: " + segment.seq + " ack: " + segment.ack + " Syn: " + segment.isSyn() + " data: " + segment.data.length);

        if (!synReceived && segment.isSyn()) {
            LOGGER.debug("[TL] [RCV] Ik ontvang voor het eerst een SYN");
            synReceived = true;
            nextAck = segment.seq + 1;
            if(synSent) {
                try {
                    LOGGER.debug("[TL] [RCV] Verbinding tussen {} en {} is nu in de state established", NetworkLayer.getLocalHost().getAddress() , receivingHost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                established = true;
            } else {
                sendSyn();
            }
            removeAckedSegment(segment);
        } else if(!established && synReceived && synSent && segment.validAck() && segment.ack == seq) {
            established = true;
            try {
                LOGGER.debug("[TL] [RCV] Verbinding tussen {} en {} is nu in de state established", NetworkLayer.getLocalHost().getAddress() , receivingHost);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (established) {
            if (segment.validSeq()) {
                if (nextAck == segment.seq) {

                    LOGGER.debug("[TL] [RCV] In-order data received");

                    receivedSegments.put(segment.seq, segment);

                    // We hebben een aaneengesloten serie gegevens.
                    Queue<Byte> data = new LinkedList<>();
                    int currentSeq = nextAck;
                    while (receivedSegments.containsKey(currentSeq)) {
                        TransportSegment currentSegment = receivedSegments.get(currentSeq);
                        data.addAll(Arrays.asList(currentSegment.data));
                        receivedSegments.remove(currentSeq);
                        currentSeq += currentSegment.data.length;
                    }

                    nextAck = currentSeq;

                    DatagramPacket packet = new DatagramPacket(ArrayUtils.toPrimitiveArray(data.toArray(new Byte[data.size()])), data.size(), receivingHost, 0);
                    adhocApplication.processPacket(packet);

                    removeAckedSegment(segment);

                    LOGGER.debug("[TL] [RCV] Unacked segment tasks: {}", unAckedSegmentTasks);


                } else if (segment.seq > nextAck) {
                    LOGGER.debug("[TL] [RCV] Out-of-order data received");
                    receivedSegments.put(segment.seq, segment);
                }


            }

            // Alleen segments acken die een syn zijn of die data bevatten. Oftewel geen acks acken.

            if(segment.isSyn() || segment.data.length > 0) {
                TransportSegment ack = new TransportSegment(new Byte[0], seq);
                ack.setAck(nextAck);
                networkLayer.send(receivingHost, ack.toByteArray());
            }



        } else if(segment.validAck()) {
            removeAckedSegment(segment);
        }

        processSendQueue();

    }


}

