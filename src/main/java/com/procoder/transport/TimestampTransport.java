package com.procoder.transport;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.procoder.Application;
import com.procoder.Network;
import com.procoder.NetworkLayer;
import com.procoder.util.AirKont;


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
        Queue<Byte> queue = sendQueues.get(dest);
        queue = queue == null ? new LinkedList<>() : queue;

        for(byte b : data) {
            queue.add(b);
        }
        sendQueues.put(dest, queue);
        processSendQueue();

    }

    @Override
    public void processPacket(DatagramPacket packet) {
        InetAddress source = packet.getAddress();
        byte[] data = packet.getData();

        System.out.println("[TL] [RCD]: " + Arrays.toString(data));

        TransportSegment receivedSegment = TransportSegment.parseNetworkData(data);

        if (receivedSegment.isDiscover()) {
            disco.addHost(packet.getAddress());
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

            List<Byte> data = new LinkedList<>();

            Iterator<Byte> it = entry.getValue().iterator();

            while (it.hasNext() && data.size() < 1400) {
                // Add byte to data to be sent
                data.add(it.next());
                // This data will be sent, so it can be removed from the queue
                it.remove();
            }
            byte[] packet = new TransportSegment(data.toArray(new Byte[data.size()])).toByteArray();
            System.out.println("[TL] [SND]: " + Arrays.toString(packet));
            networkLayer.send(null, new TransportSegment(data.toArray(new Byte[data.size()])).toByteArray());


        }

    }



}
