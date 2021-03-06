package com.procoder.routing.client;

import com.procoder.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class RoutingUDPLinkLayer {

    public static final int PORT = 31337;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingUDPLinkLayer.class);
    private MulticastSocket socket;


    public RoutingUDPLinkLayer() {
        try {
            socket = new MulticastSocket(PORT);
            socket.setLoopbackMode(false);
            socket.setSoTimeout(10);
            InetAddress multicast = InetAddress.getByName("228.0.0.0");
            socket.joinGroup(new InetSocketAddress(multicast, PORT), NetworkUtils.detectNetwork());
        } catch (IOException e) {
            LOGGER.trace("Kan socket niet openen", e);
        }
    }

    public void transmit(Packet packet) {
        InetAddress dest = packet.getDestinationAddress();
        byte [] data = packet.toByteArray();
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, dest, PORT);
        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            LOGGER.trace("Kan pakket niet verzenden", e);
        }
    }

    public Packet receive() {
        Packet result = null;
        try {
            DatagramPacket receivedPacket = new DatagramPacket(new byte[1500], 1500);
            socket.receive(receivedPacket);
            if (!receivedPacket.getAddress().equals(NetworkUtils.getLocalHost())) {
                result = processPacket(receivedPacket);
            }
        } catch (IOException e) {
            //LOGGER.trace("Geen data beschikbaar op de socket", e);
        }
        return result;
    }

    private Packet processPacket(DatagramPacket packet) {
        Packet result = null;
        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        result = Packet.parseBytes(data);

        return result;
    }

}
