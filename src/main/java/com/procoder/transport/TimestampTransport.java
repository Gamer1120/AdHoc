package com.procoder.transport;

import com.procoder.AdhocApplication;
import com.procoder.AdhocNetwork;
import com.procoder.DummyNetworkLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class TimestampTransport implements AdhocTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimestampTransport.class);


    // ------------------ Instance variables ----------------

    private AdhocNetwork networkLayer;

    private Map<InetAddress, TransportConnection> connections;

    private AdhocApplication app;

    private Discoverer disco;

    // ------------------- Constructors ---------------------

    public TimestampTransport(AdhocApplication app) {

        this.app = app;
        this.networkLayer = new DummyNetworkLayer(this);
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
        TransportConnection result = connections.getOrDefault(host,
                new TransportConnection(host, networkLayer, app));
        // Voeg de connectie toe aan de lijst (voor het geval het er nog niet in
        // zat)
        connections.put(host, result);
        return result;
    }

    // ----------------------- Commands ---------------------

    @Override
    public void send(InetAddress dest, byte[] data) {
        // Dest wordt nu genegeerd
        try {
            InetAddress broadCast = InetAddress.getByName("228.0.0.0");
            if(broadCast.equals(dest)) {
                for (InetAddress host : getKnownHostList().getKnownHosts()) {
                    TransportConnection connection = findConnection(host);

                    for (byte b : data) {
                        connection.sendByte(b);
                    }
                }
            } else {
                TransportConnection connection = findConnection(dest);

                for (byte b : data) {
                    connection.sendByte(b);
                }
            }


        } catch (UnknownHostException e) {
            LOGGER.trace("Kan het broadcastadres niet omzetten naar een InetAddress", e);
        }


        processSendQueue();
    }

    @Override
    public void processPacket(DatagramPacket packet) {
        byte[] data = packet.getData();

        TransportSegment receivedSegment = TransportSegment.parseNetworkData(data);

        if (receivedSegment.isDiscover()) {
            disco.addHost(packet.getAddress());
        } else {
            TransportConnection connection = findConnection(packet.getAddress());

            connection.receiveData(receivedSegment);
        }
    }

    @Override
    public void sendDiscovery() {
        networkLayer.send(TransportSegment.genDiscoveryPacket()
                .toByteArray());
    }

    public void processSendQueue() {
        connections.values().forEach(TransportConnection::processSendQueue);
    }
}
