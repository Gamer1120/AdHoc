package com.procoder.transport;

import com.procoder.AdhocApplication;
import com.procoder.Network;
import com.procoder.util.ArrayUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TransportConnection {

    // TODO Geval afhandelen waar de SYN verloren gaat

    private static final ScheduledThreadPoolExecutor TIMEOUT_EXECUTOR = new ScheduledThreadPoolExecutor(2);
    private static final long ACK_TIMEOUT = 1000;

// ------------------ Instance variables ----------------


    private InetAddress receivingHost;
    private HashMap<Long, ScheduledFuture> unAckedSegmentTasks;
    private AdhocApplication adhocApplication;
    private Queue<Byte> sendQueue;
    private List<Byte> receiveQueue;
    private int receiveQueueOffset;
    private Network networkLayer;
    private int seq; // Huidige sequence nummer verzendende kant
    private int nextAck; // Huidige sequence van de ontvangende kant
    private boolean synSent;
    private boolean synReceived;
    private boolean established;


// --------------------- Constructors -------------------

    public TransportConnection(InetAddress host, Network networkLayer, AdhocApplication app) {
        receivingHost = host;
        unAckedSegmentTasks = new HashMap<>();
        sendQueue = new LinkedList<>();
        receiveQueue = new LinkedList<>();
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
            System.out.println(debug + "voor de eerste keer");
        }

        final String finalDebug = debug;

        ScheduledFuture retransmitTask = TIMEOUT_EXECUTOR.scheduleAtFixedRate(() -> {
            networkLayer.send(receivingHost, syn.toByteArray());
            System.out.println(finalDebug);

        }, 0, 1000, TimeUnit.MILLISECONDS);

        unAckedSegmentTasks.put((long) (syn.seq), retransmitTask);
        networkLayer.send(receivingHost, syn.toByteArray());
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
                System.out.println("[TL] [SND]: " + Arrays.toString(segment.toByteArray()));

                // Segment is nog niet geacked dus toevoegen aan de ongeackte segments en schedule de retransmit.

                ScheduledFuture retransmitTask = TIMEOUT_EXECUTOR.scheduleAtFixedRate(() -> {
                    networkLayer.send(receivingHost, segment.toByteArray());

                }, 0, 1000, TimeUnit.MILLISECONDS);

                unAckedSegmentTasks.put((long) (segment.seq + segment.data.length), retransmitTask);
                data.clear();

            }

        }


    }

    public void removeAckedSegment(TransportSegment segment) {

        // Verwijder alle niet geackte segments met een seq + length


        Iterator<Map.Entry<Long, ScheduledFuture>> it = unAckedSegmentTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ScheduledFuture> entry = it.next();
            if (entry.getKey() < segment.ack) {
                entry.getValue().cancel(false);
                it.remove();
            }
        }

    }

    public void receiveData(TransportSegment segment) {

        System.out.println("[TL] [RCV] Processing segment  seq: " + segment.seq + " ack: " + segment.ack + " Syn: " + segment.isSyn() + " data: " + segment.data.length);

        if (!synReceived && segment.isSyn()) {
            System.out.println("[TL] [RCV] Ik ontvang voor het eerst een SYN");
            synReceived = true;
            nextAck = segment.seq + 1;
            if(synSent) {
                established = true;
            } else {
                sendSyn();
            }
            removeAckedSegment(segment);
        }

        if (established) {
            if (segment.validSeq()) {
                if (nextAck == segment.seq) {

                    System.out.println("[TL] [RCV] In-order data received");

                    for (byte b : segment.data) {
                        receiveQueue.add(b);
                    }

                    nextAck += segment.data.length;
                    // We hebben een aaneengesloten serie gegevens.
                    byte[] data = ArrayUtils.toPrimitiveArray(receiveQueue.toArray(new Byte[0]));
                    DatagramPacket packet = new DatagramPacket(data, data.length, receivingHost, 0);
                    adhocApplication.processPacket(packet);
                    receiveQueue.clear();
                    receiveQueueOffset = nextAck;

                    removeAckedSegment(segment);


                    System.out.println("[TL] [RCV] Unacked segment tasks: " + unAckedSegmentTasks);

                } else if (segment.seq > nextAck) {
                    System.out.println("[TL] [RCV] Out-of-order data received");
                    Iterator<Byte> receivedBytes = Arrays.asList(segment.data).iterator();
                    for (int i = segment.seq - receiveQueueOffset; receivedBytes.hasNext(); i++) {
                        receiveQueue.add(i, receivedBytes.next());
                    }
                }


            }

            TransportSegment ack = new TransportSegment(new Byte[0], seq);
            ack.setAck(nextAck);
            networkLayer.send(receivingHost, ack.toByteArray());

        } else if(segment.validAck()) {
            removeAckedSegment(segment);
        }

        processSendQueue();

    }


}


