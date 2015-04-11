package com.procoder.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HostList extends Observable {

    private static final int TTL = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(HostList.class);

    private ConcurrentHashMap<InetAddress, Integer> hostMap;

    public HostList() {
        hostMap = new ConcurrentHashMap<>();
    }

    public void pingReceived(InetAddress address) {
        if (!hostMap.containsKey(address)) {
            setChanged();

        }
        hostMap.put(address, TTL);
        notifyObservers(hostMap.keySet());
    }

    public Set<InetAddress> getKnownHosts() {
        return hostMap.keySet();
    }

    public void decrementTTL() {

        for (InetAddress a : hostMap.keySet()) {
            int i = hostMap.get(a) - 1;
            if (i != 0) {
                hostMap.put(a, i);
            } else {
                hostMap.remove(a);
                setChanged();
                notifyObservers(hostMap.keySet());
            }
        }

        LOGGER.debug("Hosts currently known: {}", hostMap);
    }
}
