package com.procoder.transport;

import com.procoder.AdhocApplication;
import com.procoder.NetworkLayer;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class DummyTransport implements AdhocTransport {

    private AdhocApplication app;
    private NetworkLayer networkLayer;

    public DummyTransport(AdhocApplication app) {
        this.app = app;
        this.networkLayer = new NetworkLayer(this);
    }

    @Override
    public void send(InetAddress dest, byte[] data) {
        networkLayer.send(data);
    }

    @Override
    public void processPacket(DatagramPacket packet) {
        app.processPacket(packet);
    }

    @Override
    public void sendDiscovery() {

    }

    @Override
    public HostList getKnownHostList() {
        return new HostList();
    }

}
