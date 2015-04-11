package com.procoder;

import com.procoder.transport.AdhocTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class NetworkLayer implements AdhocNetwork {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(NetworkLayer.class);
    private final static int LENGTH = 1472;
    private final static int PORT = 7777;
    private final static int IPLENGTH = 4;
    private final static int HEADER = 2 * IPLENGTH + 1;
    private AdhocTransport transportLayer;
    private InetAddress source;
    private InetAddress multicast;
    private MulticastSocket socket;
    private Map<InetAddress, Set<Byte>> packets;
    private byte id;

    public NetworkLayer(AdhocTransport transportLayer) {
        this.transportLayer = transportLayer;
        packets = new HashMap<InetAddress, Set<Byte>>();
        id = -128;
        try {
            socket = new MulticastSocket(PORT);
            socket.setLoopbackMode(false);
            source = InetAddress.getLocalHost();
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
        send(source, dest, id++, data);
    }

    private void send(InetAddress src, InetAddress dest, byte id, byte[] data) {
        // Create a packet and send it to the destination
        addPacket(src, id);
        byte[] packetData = new byte[data.length + HEADER];
        byte[] sourceAddress = src.getAddress();
        byte[] destAddress = dest.getAddress();
        packetData[0] = id;
        System.arraycopy(sourceAddress, 0, packetData, 1, IPLENGTH);
        System.arraycopy(destAddress, 0, packetData, 1 + IPLENGTH, IPLENGTH);
        System.arraycopy(data, 0, packetData, HEADER, data.length);
        DatagramPacket packet = new DatagramPacket(packetData,
                packetData.length, dest, PORT);
        LOGGER.debug("[NL] Sent packet from {} to {} with id {}",
                src.getHostAddress(), dest.getHostAddress(), id);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final NetworkInterface detectNetwork() throws SocketException {
        InetAddress source = null;
        try {
            source = NetworkLayer.getLocalHost();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Tries to find the Ad-hoc network
        NetworkInterface netIf = NetworkInterface.getByInetAddress(source);
        loop:
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface
                .getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
            NetworkInterface iface = ifaces.nextElement();
            for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses
                    .hasMoreElements(); ) {
                InetAddress address = addresses.nextElement();
                if (address.getHostName().startsWith("192.168.5.")) {
                    source = address;
                    netIf = iface;
                    break loop;
                }
            }
        }
        return netIf;
    }

    private boolean addPacket(InetAddress src, byte id) {
        synchronized (packets) {
            if (packets.containsKey(src)) {
                Set<Byte> ids = packets.get(src);
                if (ids.contains(id)) {
                    return false;
                } else {
                    ids.add(id);
                    clearPackets(ids, id);
                    return true;
                }
            } else {
                Set<Byte> ids = new HashSet<Byte>();
                ids.add(id);
                packets.put(src, ids);
                return true;
            }
        }
    }

    private void clearPackets(Set<Byte> ids, byte id) {
        for (byte i = -64; i > -128; i--) {
            ids.remove(id + i);
        }
    }

    private void removePacket(InetAddress src) {
        synchronized (packets) {
            if (packets.containsKey(src)) {
                packets.remove(src);
                LOGGER.debug("[NL] Removed {}", src.getHostAddress());
                initialPacket(src);
            } else {
                LOGGER.debug("[NL] Already removed {}", src.getHostAddress());
            }
        }
    }

    private void initialPacket(InetAddress src) {
        DatagramPacket packet = new DatagramPacket(src.getAddress(), IPLENGTH,
                multicast, PORT);
        LOGGER.debug("[NL] Sent initial packet {}", src.getHostAddress());
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receivePacket() {
        // Receive packets and forward them to the transport layer
        DatagramPacket packet = new DatagramPacket(new byte[LENGTH], LENGTH);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (packet.getLength() < HEADER) {
            byte[] data = Arrays.copyOfRange(packet.getData(), 0, IPLENGTH);
            processInitial(data);
        } else {
            byte[] data = Arrays.copyOfRange(packet.getData(), 0,
                    packet.getLength());
            processPacket(packet, data);
        }
    }

    private void processInitial(byte[] data) {
        InetAddress src = null;
        try {
            src = InetAddress.getByAddress(Arrays.copyOfRange(data, 1,
                    IPLENGTH + 1));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        LOGGER.debug("[NL] Ininital packet from {}", src.getHostAddress());
        removePacket(src);
    }

    private void processPacket(DatagramPacket packet, byte[] data) {
        byte packetId = data[0];

        InetAddress src = null;
        InetAddress dest = null;
        try {
            src = InetAddress.getByAddress(Arrays.copyOfRange(data, 1,
                    IPLENGTH + 1));
            dest = InetAddress.getByAddress(Arrays.copyOfRange(data,
                    IPLENGTH + 1, HEADER));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        LOGGER.debug("[NL] Received packet from {} to {} with id {}",
                src.getHostAddress(), dest.getHostAddress(), packetId);
        data = Arrays.copyOfRange(data, HEADER, data.length);
        if (addPacket(src, packetId)) {
            if (multicast.equals(dest)) {
                send(src, dest, packetId, data);
                packet.setData(data);
                transportLayer.processPacket(packet);
            } else if (source.equals(dest)) {
                packet.setData(data);
                transportLayer.processPacket(packet);
            } else {
                send(src, dest, packetId, data);
            }
        }
    }

    @Override
    public void run() {
        initialPacket(source);
        while (true) {
            receivePacket();
        }
    }
}
