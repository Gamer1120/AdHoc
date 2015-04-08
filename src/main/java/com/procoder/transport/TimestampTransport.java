package com.procoder.transport;

import com.procoder.Application;
import com.procoder.Network;
import com.procoder.NetworkLayer;
import com.procoder.util.AirKont;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;


public class TimestampTransport implements Transport {

    /**
     *
     */

    // ------------------ Instance variables ----------------

    private Network networkLayer;

    private Map<InetAddress, Queue<TransportSegment>> unAckedSegments;
    private Map<InetAddress, Queue<Byte>> sendQueues;

    private Application app;

    private Discoverer disco;

    // ------------------- Constructors ---------------------

    public TimestampTransport(Application app) {

        this.app = app;
        this.networkLayer = new NetworkLayer(this);
        this.sendQueues=new HashMap<>();
        this.unAckedSegments = new HashMap<>();
        new Thread(networkLayer).start();
        disco = new Discoverer(this);

    }


    // ----------------------- Queries ----------------------

    @Override
    public HostList getKnownHostList() {
        return disco.getHostList();
    }


    // ----------------------- Commands ---------------------


    @Override
    public void send(InetAddress dest, byte[] data) {
        // Dest wordt nu genegeerd
        for(InetAddress host : getKnownHostList().getKnownHosts()) {
            Queue<Byte> queue = sendQueues.get(host);
            queue = queue == null ? new LinkedList<>() : queue;

            for(byte b : data) {
                queue.add(b);
            }
            sendQueues.put(host, queue);
        }

        processSendQueue();

    }

    @Override
    public void processPacket(DatagramPacket packet) {
        byte[] data = packet.getData();

        System.out.println("[TL] [RCD]: " + Arrays.toString(data));

        TransportSegment receivedSegment = TransportSegment.parseNetworkData(data);

        if (receivedSegment.isDiscover()) {
            disco.addHost(packet.getAddress());
            System.out.println("[TL] Received discovery packet for address" + packet.getAddress());
        } else {
            packet.setData(AirKont.toPrimitiveArray(receivedSegment.data));
            app.processPacket(packet);
        }



    }

    @Override
    public void sendDiscovery() {
        TransportSegment discoverSegment = new TransportSegment(new Byte[0]);
        discoverSegment.setDiscover();
        networkLayer.send(null, discoverSegment.toByteArray());
    }

    public void processSendQueue() {

        for(Map.Entry<InetAddress, Queue<Byte>> entry : sendQueues.entrySet()) {

            InetAddress address = entry.getKey();

            List<Byte> data = new LinkedList<>();

            Iterator<Byte> it = entry.getValue().iterator();

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
                networkLayer.send(null, segment.toByteArray());


                // Segment is nog niet geacked dus toevoegen aan de ongeackte segments.
                Queue<TransportSegment> segmentQueue = unAckedSegments.get(address);
                segmentQueue = segmentQueue == null ? new LinkedList<>() : segmentQueue;
                segmentQueue.add(segment);

            }




        }

    }



}
