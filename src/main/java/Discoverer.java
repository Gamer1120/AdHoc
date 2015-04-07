

import java.net.Inet4Address;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by reneb_000 on 7-4-2015.
 */
public class Discoverer extends TimerTask {

    Timer timer;

    private HashMap<String, Integer> hostMap = new HashMap<String,Integer>();
    private Set<String> knownHosts = new HashSet<String>();
    private int TTL = 10;
    private int TDI = 5; //Time Discovery Intveral
    private int counter = 0;


    public Discoverer(){
        timer = new Timer();
        timer.schedule(this, 0, 1000);
        addHost("test");
    }


    public void addHost(String address){
        knownHosts.add(address);
        hostMap.put(address, TTL);
    }


    @Override
    public void run() {
        counter ++;
        System.out.println("TICK");
        for(String a:hostMap.keySet()){
            Integer i = hostMap.get(a) -1;
            System.out.println(a +"TTL is "+i);
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

    public static void main(String[] args){
        new Discoverer();
    }



}
