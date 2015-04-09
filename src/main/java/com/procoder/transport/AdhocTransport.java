package com.procoder.transport;

import java.net.DatagramPacket;
import java.net.InetAddress;

public interface AdhocTransport {

    public void send(InetAddress dest, byte[] data);

    public void processPacket(DatagramPacket packet);

    public void sendDiscovery();

    public HostList getKnownHostList();

}
