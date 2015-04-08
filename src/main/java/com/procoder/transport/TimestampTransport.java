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

    private Queue<TransportSegment> unAckedData;

    private Map<InetAddress, Queue<Byte>> sendQueues;

    private Application app;

    private Discoverer disco;

    // ------------------- Constructors ---------------------

    public TimestampTransport(Application app) {

        this.app = app;
        this.networkLayer = new NetworkLayer(this);
        this.sendQueues=new HashMap<InetAddress,Queue<Byte>>();
        new Thread(networkLayer).start();
        disco = new Discoverer(this);

    }


    // ----------------------- Queries ----------------------


    // ----------------------- Commands ---------------------


    @Override
    public void send(InetAddress dest, byte[] data) {
        Queue<Byte> queue = sendQueues.get(dest);
        queue = queue == null ? new LinkedList<Byte>() : queue;

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

        System.out.println("[TL] Received: " + Arrays.toString(data));

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

    @Override
    public HostList getKnownHostList() {
        return disco.getHostList();
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
            System.out.println("[TL] Sending: " + Arrays.toString(packet));
            networkLayer.send(null, new TransportSegment(data.toArray(new Byte[data.size()])).toByteArray());


        }

    }



}
