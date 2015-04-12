package com.procoder.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils {


    public static InetAddress getLocalHost() throws IOException {
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

    public static NetworkInterface detectNetwork() throws SocketException {
        InetAddress source = null;
        try {
            source = getLocalHost();
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

    public static Inet4Address getBroadcastAddress() {
        Inet4Address result = null;
        try {
            result = (Inet4Address) Inet4Address.getByName("228.0.0.0");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
