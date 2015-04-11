package com.procoder;

import com.procoder.transport.AdhocTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class DummyNetworkLayer implements AdhocNetwork {
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

    public DummyNetworkLayer(AdhocTransport transportLayer) {
        this.transportLayer = transportLayer;
        try {
            socket = new MulticastSocket(PORT);
            socket.setLoopbackMode(false);
            localAddress = InetAddress.getLocalHost();
            multicast = InetAddress.getByName("228.0.0.0");
            socket.joinGroup(new InetSocketAddress(multicast, PORT),
                    detectNetwork());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final static InetAddress getLocalHost() throws IOException {
        InetAddress localHost = InetAddress.getLocalHost();
        loop:
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface
                .getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
            for (Enumeration<InetAddress> addresses = ifaces.nextElement()
                    .getInetAddresses(); addresses.hasMoreElements(); ) {
                InetAddress address = addresses.nextElement();
                if (address.getHostName().startsWith("192.168.5.")) {
                    localHost = address;
                    break loop;
                }
            }
        }
        return localHost;
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
        System.arraycopy(sourceAddress, 0, packetData, 1, IPLENGTH);
        System.arraycopy(destAddress, 0, packetData, 1 + IPLENGTH, IPLENGTH);
        System.arraycopy(data, 0, packetData, HEADER, data.length);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, dest, PORT);
        try {
            socket.send(packet);
            LOGGER.debug("[NL] Sent packet from {} to {}", src.getHostAddress(), dest.getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private NetworkInterface detectNetwork() throws SocketException {
        // Tries to find the Ad-hoc network
        NetworkInterface netIf = NetworkInterface.getByInetAddress(localAddress);
        loop:
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface
                .getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
            NetworkInterface iface = ifaces.nextElement();
            for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses
                    .hasMoreElements(); ) {
                InetAddress address = addresses.nextElement();
                if (address.getHostName().startsWith("192.168.5.")) {
                    localAddress = address;
                    netIf = iface;
                    break loop;
                }
            }
        }
        return netIf;
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
            InetAddress src = InetAddress.getByAddress(Arrays.copyOfRange(data, 0,
                    IPLENGTH));
            InetAddress dest = InetAddress.getByAddress(Arrays.copyOfRange(data,
                    IPLENGTH, HEADER));
            LOGGER.debug("[NL] Received packet from {} to {}",
                    src.getHostAddress(), dest.getHostAddress());
            data = Arrays.copyOfRange(data, HEADER, data.length);
            if (multicast.equals(dest) || localAddress.equals(dest)) {
                packet.setData(data);
                transportLayer.processPacket(packet);
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
