package com.procoder;

import java.net.Inet4Address;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class Discoverer extends TimerTask {

    Timer timer;

    private HashMap<Inet4Address, Integer> hostMap = new HashMap<Inet4Address,Integer>();
    private Set<Inet4Address> knownHosts = new HashSet<Inet4Address>();
    private int TTL = 10;
    private int TDI = 5; //Time Discovery Intveral
    private int counter = 0;


    public Discoverer(){
        timer = new Timer();
        timer.schedule(this, 0, 1000);
    }


    public void addHost(Inet4Address address){
        knownHosts.add(address);
        hostMap.put(address, TTL);
    }


    @Override
    public void run() {
        counter ++;
        for(Inet4Address a:hostMap.keySet()){
            Integer i = hostMap.get(a) -1;
            if(i!=0){
                hostMap.put(a, i);
            }else{
                hostMap.remove(a);
            }


        }

        if(counter == TDI){
            //TODO Send discover thingy
            counter = 0;
        }
    }




}
