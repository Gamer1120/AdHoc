package com.procoder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

import com.procoder.transport.Transport;

public class NetworkLayer implements Network {
    // TODO routing tables
    private final static int LENGTH = 1472;
    private final static int PORT = 7777;
    private final static int IPLENGTH = 4;
    private final static int HEADER = 2 * IPLENGTH + 1;
    private final static byte TTL = 4;
    private Transport transportLayer;
    private InetAddress source;
    private InetAddress multicast;
    private MulticastSocket socket;

    public NetworkLayer(Transport transportLayer) {
        this.transportLayer = transportLayer;
        try {
            socket = new MulticastSocket(PORT);
            source = InetAddress.getLocalHost();
            multicast = InetAddress.getByName("228.0.0.0");
            socket.joinGroup(new InetSocketAddress(multicast, PORT),
                    detectNetwork());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(InetAddress dest, byte[] data) {
        send(dest, data, TTL);
    }

    private void send(InetAddress dest, byte[] data, byte ttl) {
        // Create a packet and send it to the destination
        if (dest == null) {
            dest = multicast;
        }
        byte[] packetData = new byte[data.length + HEADER];
        packetData[0] = ttl;
        byte[] sourceAddress = source.getAddress();
        byte[] destAddress = dest.getAddress();
        System.arraycopy(sourceAddress, 0, packetData, 1, IPLENGTH);
        System.arraycopy(destAddress, 0, packetData, 1 + IPLENGTH, IPLENGTH);
        System.arraycopy(data, 0, packetData, HEADER, data.length);

        DatagramPacket packet = new DatagramPacket(packetData,
                packetData.length, dest, PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private NetworkInterface detectNetwork() throws SocketException {
        // Tries to find the Ad-hoc network
        NetworkInterface netIf = NetworkInterface.getByInetAddress(source);
        loop: for (Enumeration<NetworkInterface> ifaces = NetworkInterface
                .getNetworkInterfaces(); ifaces.hasMoreElements();) {
            NetworkInterface iface = ifaces.nextElement();
            for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses
                    .hasMoreElements();) {
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

    @Override
    public void run() {
        // Receive packets and forward them to the transport layer
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[LENGTH], LENGTH);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] data = Arrays.copyOfRange(packet.getData(), 0,
                    packet.getLength());
            byte ttl = data[0];

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
            data = Arrays.copyOfRange(data, HEADER, data.length);

            if (!source.equals(src)
                    && (multicast.equals(dest) || source.equals(dest))) {
                packet.setData(data);
                transportLayer.processPacket(packet);
            }
            if (--ttl > 0 && !source.equals(dest)) {
                send(dest, data, ttl);
            }
        }
    }
}
