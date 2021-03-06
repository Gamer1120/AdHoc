package com.procoder.routing.client;

import com.procoder.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Basic implementation of AbstractRoute.
 * @author Jaco
 * @version 09-03-2015
 */
public class BasicRoute extends AbstractRoute {

    public static final Logger LOGGER = LoggerFactory.getLogger(BasicRoute.class);

    public byte distance;
    public Inet4Address destination;
    //Invariant path.length < 30;
    public Inet4Address[] path;
    public byte costToNext;


    public BasicRoute(Inet4Address destination, Inet4Address nextHop, byte distance, byte costToNext, Inet4Address[] path) {
        if (path.length >= 30) {
            throw new RuntimeException("path is too long, i'm out");
        }
        this.destination = destination;
        this.distance = distance;
        this.nextHop = nextHop;
        this.path = path;
        this.costToNext = costToNext;
    }

    /**
     * @param bytes De bytes gegenereerd door toByteArray minus de eerste byte voor de lengte
     * @return
     */
    public static BasicRoute parseBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        byte distance = buf.get();
        byte costToNext = buf.get();
        Inet4Address nextHop = null;
        Inet4Address destination = null;
        try {
            nextHop = BufferUtils.readI4Address(buf);
            destination = BufferUtils.readI4Address(buf);
        } catch (UnknownHostException e) {
            LOGGER.debug("Cannot parse the bytes to a valid Route");
        }

        List<Inet4Address> tempRoute = new LinkedList<>();

        while (buf.remaining() >= 4) {
            try {
                tempRoute.add(BufferUtils.readI4Address(buf));
            } catch (UnknownHostException e) {
                LOGGER.debug("Cannot parse the bytes to a valid IP Adress");
            }
        }

        return new BasicRoute(destination, nextHop, distance, costToNext, tempRoute.toArray(new Inet4Address[tempRoute.size()]));


    }

    public boolean routeContains(Inet4Address addr) {
        return Arrays.asList(path).contains(addr);
    }

    public byte[] toByteArray() {
        // Lengte, path, nextHop, destination, en costToNext
        byte rowSize = (byte) (1 + path.length * 4 + 4 + 4 + 1);
        // De eerste byte is de lengte van de rest van de toko van deze rij;
        ByteBuffer buf = ByteBuffer.allocate(rowSize + 1);
        buf.put(rowSize);
        buf.put(distance);
        buf.put(costToNext);

        byte[] nextHopBytes = nextHop.getAddress();
        buf.put(nextHopBytes);

        byte[] destBytes = destination.getAddress();
        buf.put(destBytes);

        for (Inet4Address address : path) {
            byte[] bytes = address.getAddress();
            buf.put(bytes);
        }


        return buf.array();
    }

    @Override
    public String toString() {

        return "Next hop: " + nextHop + " Distance: " + distance + " CostToNext: " + costToNext + " Route: " + Arrays.toString(path);
    }


}
