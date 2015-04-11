package com.procoder.routing.protocol;

import com.procoder.routing.client.IRoutingProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingService.class);

    private Class<? extends IRoutingProtocol> routingImpl;
    private Thread routingThread;

    public RoutingService(Class<? extends IRoutingProtocol> routingImpl) {
        this.routingImpl = routingImpl;
    }

    private void start() {
        routingThread = new Thread(createProtocol(), "Routing Thread");
        routingThread.start();

    }

    public void stop() {
        // TODO stop routing thread
    }

    private IRoutingProtocol createProtocol() {
        IRoutingProtocol result = null;
        try {
            result = routingImpl.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.trace("Kan geen instantie maken van de routing protocol implementatie", e);
        }
        return result;
    }
}
