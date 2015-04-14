package com.procoder.transport;

import com.procoder.AdhocApplication;
import com.procoder.AdhocNetwork;
import com.procoder.util.ArrayUtils;
import com.procoder.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

public class TransportConnection {

    private static final ScheduledThreadPoolExecutor TIMEOUT_EXECUTOR = new ScheduledThreadPoolExecutor(2);
    private static final long ACK_TIMEOUT = 1000;
    // FIXME Dit kan beter worden bijgehouden in bytes in plaats van aantal segments
    private static final int MAX_UNACK_SEG = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportConnection.class);

// ------------------ Instance variables ----------------


    private InetAddress receivingHost;
    private HashMap<Long, ScheduledFuture> unAckedSegmentTasks;
    private AdhocApplication adhocApplication;
    private BlockingQueue<Byte> sendQueue;
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
        sendQueue = new LinkedBlockingQueue<>();
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

        }, 0, ACK_TIMEOUT, TimeUnit.MILLISECONDS);

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

            // Hier wordt ervoor gezord dat de send window niet overschreden wordt
            while (it.hasNext() && unAckedSegmentTasks.size() < MAX_UNACK_SEG) {

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

                }, 0, ACK_TIMEOUT, TimeUnit.MILLISECONDS);

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

        /*
        Verschillende gevallen:
        * SYN wanneer nog geen syn ontvangen
        * SYN ACK wanneer nog geen syn ontvangen en een syn verstuurd
        * ACK op een syn ack
        * In order data wanneer established
        * Out of order data wanneer established
        * Andere gevallen stuur een reset.
        * Waneer een reset ontvangen, zet established, synSent en synReceived op false.
         */

        LOGGER.debug("[TL] [RCV] Processing segment  seq: " + segment.seq + " ack: " + segment.ack + " Syn: " + segment.isSyn() + " data: " + segment.data.length);

        boolean ackSent = processFlags(segment);

        if (established && segment.validAck() && nextAck == segment.seq) {
            // In order data wanneer established
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
            } else {
                // Out of order data wanneer established
                // TODO Alleen binnen een bepaalde range in de buffer zetten
                LOGGER.debug("[TL] [RCV] Out-of-order data received");
                receivedSegments.put(segment.seq, segment);
            }
        }

        // Syn en segments die data bevatten ACKEN
        if (!ackSent && segment.isSyn() || segment.data.length > 0) {
            TransportSegment ack = new TransportSegment(new Byte[0], seq);
            ack.setAck(nextAck);
            networkLayer.send(receivingHost, ack.toByteArray());
        }

        removeAckedSegment(segment);
        processSendQueue();

    }

    private boolean processFlags(TransportSegment segment) {
        boolean ackSent = false;
        if (segment.isRST()) {
            // Reset ontvangen
            LOGGER.debug("RESET Ontvangen");
            established = false;
            synSent = false;
            synReceived = false;
            sendSyn();
        } else if (segment.isSyn() && !synReceived && !synSent) {
            // SYN wanneer nog geen syn ontvangen
            LOGGER.debug("[TL] [RCV] Ik ontvang voor het eerst een SYN");
            synReceived = true;
            nextAck = segment.seq + 1;
            sendSyn();
            removeAckedSegment(segment);
            ackSent = true;

        } else if (segment.isSyn() && segment.validAck() && synSent && !synReceived) {
            //SYN ACK wanneer nog geen syn ontvangen en een syn verstuurd
            LOGGER.debug("[TL] [RCV] Verbinding tussen {} en {} is nu in de state established", NetworkUtils.getLocalHost().getHostAddress(), receivingHost);
            nextAck = segment.seq + 1; // SYN ACK neemt een data byte in beslag.
            synReceived = true;
            established = true;
            // ACK sturen
        } else if (!established && synReceived && synSent && segment.validAck() && segment.ack == seq) {
            // ACK op een syn ack
            established = true;
            LOGGER.debug("[TL] [RCV] Verbinding tussen {} en {} is nu in de state established", NetworkUtils.getLocalHost().getHostAddress(), receivingHost);
        } else if (!established && !synSent && !synReceived) {
            // Andere gevallen stuur een reset.
            LOGGER.debug("Ik stuur nu een RESET");
            TransportSegment syn = new TransportSegment(new Byte[0], seq);
            syn.setRST();
            networkLayer.send(receivingHost, syn.toByteArray());
        }

        return ackSent;
    }


}

