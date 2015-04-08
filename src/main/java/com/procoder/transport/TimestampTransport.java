package com.procoder.transport;

import com.procoder.Application;
import com.procoder.Network;
import com.procoder.NetworkLayer;
import com.procoder.util.ArrayUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;


public class TimestampTransport implements Transport {


    // ------------------ Instance variables ----------------

    private Network networkLayer;

    private Map<InetAddress, TransportConnection> connections;

    private Application app;

    private Discoverer disco;

    // ------------------- Constructors ---------------------

    public TimestampTransport(Application app) {

        this.app = app;
        this.networkLayer = new NetworkLayer(this);
        this.connections = new HashMap<>();
        new Thread(networkLayer).start();
        disco = new Discoverer(this);

    }


    // ----------------------- Queries ----------------------

    @Override
    public HostList getKnownHostList() {
        return disco.getHostList();
    }

    private TransportConnection findConnection(InetAddress host) {

        // De connectie is misschien nog niet opgezet
        TransportConnection result = connections.getOrDefault(host, new TransportConnection(host, networkLayer));
        // Voeg de connectie toe aan de lijst (voor het geval het er nog niet in zat)
        connections.put(host, result);
        return result;
    }


    // ----------------------- Commands ---------------------


    @Override
    public void send(InetAddress dest, byte[] data) {
        // Dest wordt nu genegeerd
        for(InetAddress host : getKnownHostList().getKnownHosts()) {
            TransportConnection connection = findConnection(host);

            for(byte b : data) {
                connection.sendByte(b);
            }
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
            packet.setData(ArrayUtils.toPrimitiveArray(receivedSegment.data));
            app.processPacket(packet);
        }



    }

    @Override
    public void sendDiscovery() {
        networkLayer.send(null, TransportSegment.genDiscoveryPacket().toByteArray());
    }

    public void processSendQueue() {

        connections.values().forEach(TransportConnection::processSendQueue);

    }



}
