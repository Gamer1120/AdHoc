package com.procoder.routing.protocol;

import com.procoder.routing.client.AbstractRoute;
import com.procoder.routing.client.IRoutingProtocol;
import com.procoder.routing.client.LinkLayer;
import com.procoder.routing.client.RoutingUDPLinkLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.util.concurrent.ConcurrentHashMap;

public class RoutingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingService.class);

    private IRoutingProtocol routingImpl;
    private Thread routingThread;

    public RoutingService(IRoutingProtocol routingImpl) {
        this.routingImpl = routingImpl;
        routingImpl.init(new LinkLayer(new RoutingUDPLinkLayer()));
        start();
    }

    private void start() {
        routingThread = new Thread(routingImpl, "Routing Thread");
        routingThread.start();
    }

    public void stop() {
        routingImpl.stop();
    }

    public ConcurrentHashMap<Inet4Address, ? extends AbstractRoute> getForwardingTable() {
        return routingImpl.getForwardingTable();
    }
}
