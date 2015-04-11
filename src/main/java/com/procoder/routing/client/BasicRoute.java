package com.procoder.routing.client;

import java.util.Arrays;

/**
 * Basic implementation of AbstractRoute.
 * @author Jaco
 * @version 09-03-2015
 */
public class BasicRoute extends AbstractRoute {

    public int distance;
    public Integer[] route;
    public int costToNext;


    public BasicRoute(int nextHop, int distance, int costToNext, Integer[] route) {
        this.distance = distance;
        this.nextHop = nextHop;
        this.route = route;
        this.costToNext = costToNext;
    }

    public boolean routeContains(int addr) {
        return Arrays.asList(route).contains(addr);
    }





}
