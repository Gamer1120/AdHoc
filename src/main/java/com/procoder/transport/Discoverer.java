package com.procoder.transport;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class Discoverer extends TimerTask {

    Timer timer;

    private static final int TDI = 5; //Time Discovery Intveral
    private int counter = 0;
    private Transport transport;
    private HostList hostList;


    public Discoverer(Transport transport){
        this.transport = transport;
        hostList = new HostList();
        timer = new Timer();
        timer.schedule(this, 0, 1000);
    }


    public void addHost(InetAddress address){
        hostList.pingReceived(address);
    }

    public HostList getHostList() {
        return hostList;
    }




    @Override
    public void run() {
        counter ++;
        hostList.decrementTTL();

        if(counter == TDI){
            transport.sendDiscovery();
            counter = 0;
        }
    }




}
