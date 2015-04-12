package com.procoder;

import com.procoder.routing.client.AbstractRoute;
import com.procoder.routing.client.BasicRoute;
import com.procoder.routing.protocol.RIPRoutingProtocol;
import com.procoder.routing.protocol.RoutingService;
import com.procoder.transport.AdhocTransport;
import com.procoder.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Map;

public class ForwardingNetworkLayer implements AdhocNetwork {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(NetworkLayer.class);
    private final static int LENGTH = 1472;
    private final static int PORT = 7777;
    private final static int IPLENGTH = 4;
    private final static int HEADER = 2 * IPLENGTH;
    private AdhocTransport transportLayer;
    private InetAddress localAddress;
    private InetAddress multicast;
    private MulticastSocket socket;

    private RoutingService routingService;

    public ForwardingNetworkLayer(AdhocTransport transportLayer) {
        this.transportLayer = transportLayer;
        try {
            socket = new MulticastSocket(PORT);
            socket.setLoopbackMode(false);
            localAddress = NetworkUtils.getLocalHost();
            multicast = InetAddress.getByName("228.0.0.0");
            socket.joinGroup(new InetSocketAddress(multicast, PORT),
                    NetworkUtils.detectNetwork());
        } catch (IOException e) {
            e.printStackTrace();
        }

        routingService = new RoutingService(new RIPRoutingProtocol());
    }

    @Override
    public void send(byte[] data) {
        send(multicast, data);
    }

    @Override
    public void send(InetAddress dest, byte[] data) {
        send(localAddress, dest, data);
    }

    private void send(InetAddress src, InetAddress dest, byte[] data) {
        byte[] packetData = new byte[data.length + HEADER];
        byte[] sourceAddress = src.getAddress();
        byte[] destAddress = dest.getAddress();
        System.arraycopy(sourceAddress, 0, packetData, 0, IPLENGTH);
        System.arraycopy(destAddress, 0, packetData, IPLENGTH, IPLENGTH);
        System.arraycopy(data, 0, packetData, HEADER, data.length);


        BasicRoute route = (BasicRoute) routingService.getForwardingTable().get(dest);
        Inet4Address nextHop = route != null ? route.nextHop : null;
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, nextHop, PORT);

        if (NetworkUtils.getBroadcastAddress().equals(dest)) {
            packet.setAddress(dest);
        }

        try {
            if (packet.getAddress() != null) {
                socket.send(packet);
                LOGGER.debug("[NL] Sent packet from {} to {} via {}", src.getHostAddress(), dest.getHostAddress(), packet.getAddress());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receivePacket() {
        // Receive packets and forward them to the transport layer
        DatagramPacket packet = new DatagramPacket(new byte[LENGTH], LENGTH);
        try {
            socket.receive(packet);
            byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            processPacket(packet, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processPacket(DatagramPacket packet, byte[] data) {
        try {
            byte[] noHeaderdata;
            InetAddress src = InetAddress.getByAddress(Arrays.copyOfRange(data, 0,
                    IPLENGTH));
            InetAddress dest = InetAddress.getByAddress(Arrays.copyOfRange(data,
                    IPLENGTH, HEADER));
            LOGGER.debug("[NL] Received packet from {} to {}",
                    src.getHostAddress(), dest.getHostAddress());
            noHeaderdata = Arrays.copyOfRange(data, HEADER, data.length);

            // Pakketten voor dit adres die niet verzonden zijn door onszelf mogen naar de transportLayer

            if (!src.equals(localAddress) && (multicast.equals(dest) || localAddress.equals(dest))) {
                DatagramPacket transPacket = new DatagramPacket(Arrays.copyOf(noHeaderdata, noHeaderdata.length), noHeaderdata.length);
                transPacket.setAddress(src);
                transportLayer.processPacket(transPacket);
            }

            // Pakketten voor multicast moeten doorgestuurd worden naar computers die het nog niet hebben.
            if (dest.equals(NetworkUtils.getBroadcastAddress())) {
                for (Map.Entry<Inet4Address, ? extends AbstractRoute> ipRoutePair : routingService.getForwardingTable().entrySet()) {
                    // Hoeft alleen door te sturen als het adres niet de laatste hop van het pakket is en als de path naar
                    // de bestemming niet via de laatste hop van het pakket gaat.
                    BasicRoute route = (BasicRoute) ipRoutePair.getValue();

                    if (!ipRoutePair.getKey().equals(localAddress) && !route.nextHop.equals(packet.getAddress()) && !Arrays.asList(route.path).contains(packet.getAddress())) {
                        DatagramPacket forwardingPacket = new DatagramPacket(Arrays.copyOf(data, data.length), data.length, route.destination, PORT);
                        // TODO dit samenvoegen met de gewone send methode
                        try {
                            socket.send(forwardingPacket);
                            LOGGER.debug("Stuur broadcast pakket door naar {}", forwardingPacket.getAddress());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            } else {
                // Forward het pakket naar het juiste adres.
                BasicRoute route = (BasicRoute) routingService.getForwardingTable().get(dest);
                DatagramPacket forwardingPacket = new DatagramPacket(Arrays.copyOf(data, data.length), data.length, route.nextHop, PORT);
                try {
                    socket.send(forwardingPacket);
                    LOGGER.debug("Stuur unicast pakket voor {} door naar {}", dest, route.nextHop);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            receivePacket();
        }
    }
}
