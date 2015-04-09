package com.procoder.transport;

import com.procoder.Application;
import com.procoder.Network;
import com.procoder.util.ArrayUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;

public class TransportConnection {

// ------------------ Instance variables ----------------


    private InetAddress receivingHost;
    private Queue<TransportSegment> unAckedSegments;
    private Application application;
    private Queue<Byte> sendQueue;
    private Queue<Byte> receiveQueue;
    private Network networkLayer;
    private int seq;
    private int nextAck;
    private boolean established;
    private boolean synReceived;




// --------------------- Constructors -------------------

    public TransportConnection (InetAddress host, Network networkLayer, Application app) {
        receivingHost = host;
        unAckedSegments = new LinkedList<>();
        sendQueue = new LinkedList<>();
        this.networkLayer = networkLayer;
        seq = new Random().nextInt();
        established = false;
        synReceived = false;
        application = app;
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
            TransportSegment segment = new TransportSegment(data.toArray(new Byte[data.size()]), seq);
            if (!established) {
                segment.setSyn();
                established = true;
            }
            if (synReceived) {
                segment.setAck(nextAck);
            }
            seq += data.size();
            System.out.println("[TL] [SND]: " + Arrays.toString(segment.toByteArray()));
            networkLayer.send(receivingHost, segment.toByteArray());


            // Segment is nog niet geacked dus toevoegen aan de ongeackte segments.
            unAckedSegments.add(segment);

        }
    }

    public void receiveData(TransportSegment segment) {

        if(!synReceived) {
            synReceived = segment.isSyn();
            if (synReceived) {
                nextAck = segment.seq + segment.data.length;
            }
        }

        // Dit werkt nog niet voor out of order data

        if(synReceived) {
            if(segment.validSeq()) {
                for(byte b : segment.data) {
                    receiveQueue.add(b);
                }
                if(nextAck == segment.seq) {
                    nextAck += segment.data.length;
                    // We hebben een aaneengesloten serie gegevens.
                    byte[] data = ArrayUtils.toPrimitiveArray(receiveQueue.toArray(new Byte[0]));
                    DatagramPacket packet = new DatagramPacket(data, data.length, receivingHost, 0);
                    application.processPacket(packet);

                }
            }
        }




    }

}
