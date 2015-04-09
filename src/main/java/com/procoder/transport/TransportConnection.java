package com.procoder.transport;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.procoder.AdhocApplication;
import com.procoder.Network;
import com.procoder.util.ArrayUtils;

public class TransportConnection {

    // ------------------ Instance variables ----------------

    private InetAddress receivingHost;
    private Queue<TransportSegment> unAckedSegments;
    private AdhocApplication adhocApplication;
    private Queue<Byte> sendQueue;
    private Queue<Byte> receiveQueue;
    private Network networkLayer;
    private int seq;
    private int nextAck;
    private boolean established;
    private boolean synReceived;

    // --------------------- Constructors -------------------

    public TransportConnection(InetAddress host, Network networkLayer,
            AdhocApplication app) {
        receivingHost = host;
        unAckedSegments = new LinkedList<>();
        sendQueue = new LinkedList<>();
        receiveQueue = new LinkedList<>();
        this.networkLayer = networkLayer;
        seq = new Random().nextInt();
        established = false;
        synReceived = false;
        adhocApplication = app;
    }

    // ----------------------- Queries ----------------------

    // ----------------------- Commands ---------------------

    public void sendByte(byte b) {
        sendQueue.add(b);
    }

    public void processSendQueue() {

        List<Byte> data = new LinkedList<>();

        Iterator<Byte> it = sendQueue.iterator();

        while (it.hasNext()) {

            while (it.hasNext() && data.size() < 1400) {
                // Add byte to data to be sent
                data.add(it.next());
                // This data will be sent, so it can be removed from the queue
                it.remove();
            }
            TransportSegment segment = new TransportSegment(
                    data.toArray(new Byte[data.size()]), seq);
            if (!established) {
                segment.setSyn();
                established = true;
            }
            if (synReceived) {
                segment.setAck(nextAck);
            }
            seq += data.size();

            networkLayer.send(receivingHost, segment.toByteArray());

            // Segment is nog niet geacked dus toevoegen aan de ongeackte
            // segments.
            unAckedSegments.add(segment);
            data.clear();
        }
    }

    public void receiveData(TransportSegment segment) {
        if (!synReceived) {
            synReceived = segment.isSyn();
            nextAck = segment.seq;
        }

        // Dit werkt nog niet voor out of order data
        if (synReceived) {
            if (segment.validSeq()) {
                for (byte b : segment.data) {
                    receiveQueue.add(b);
                }
                if (nextAck == segment.seq) {
                    nextAck += segment.data.length;
                    // We hebben een aaneengesloten serie gegevens.
                    byte[] data = ArrayUtils.toPrimitiveArray(receiveQueue
                            .toArray(new Byte[0]));
                    DatagramPacket packet = new DatagramPacket(data,
                            data.length, receivingHost, 0);
                    adhocApplication.processPacket(packet);
                    receiveQueue.clear();
                    nextAck = segment.seq + segment.data.length;

                }
            }
        }
    }
}
