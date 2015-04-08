package com.procoder.transport;

import com.procoder.Network;

import java.net.InetAddress;
import java.util.*;

public class TransportConnection {

// ------------------ Instance variables ----------------


    private InetAddress receivingHost;
    private Queue<TransportSegment> unAckedSegments;
    private Queue<Byte> sendQueue;
    private Network networkLayer;


// --------------------- Constructors -------------------

    public TransportConnection (InetAddress host, Network networkLayer) {
        receivingHost = host;
        unAckedSegments = new LinkedList<>();
        sendQueue = new LinkedList<>();
        this.networkLayer = networkLayer;
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
            byte[] packet = new TransportSegment(data.toArray(new Byte[data.size()])).toByteArray();
            System.out.println("[TL] [SND]: " + Arrays.toString(packet));
            TransportSegment segment = new TransportSegment(data.toArray(new Byte[data.size()]));
            networkLayer.send(receivingHost, segment.toByteArray());


            // Segment is nog niet geacked dus toevoegen aan de ongeackte segments.
            unAckedSegments.add(segment);

        }
    }

}
