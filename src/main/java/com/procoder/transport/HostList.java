package com.procoder.transport;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Observable;

public class HostList extends Observable {

    private static final int TTL = 10;

    private HashMap<InetAddress, Integer> hostMap;

    public HostList() {
        hostMap = new HashMap<>();
    }

    public void pingReceived(InetAddress address) {
        if(!hostMap.containsKey(address)) {
            setChanged();
        }
        hostMap.put(address, TTL);
        notifyObservers(hostMap.keySet());
    }

    public void decrementTTL() {
        for(InetAddress a: hostMap.keySet()){
            int i = hostMap.get(a) -1;
            if(i!=0){
                hostMap.put(a, i);
            }else{
                hostMap.remove(a);
                setChanged();
                notifyObservers(hostMap.keySet());
            }


        }
    }


}
