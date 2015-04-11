package com.procoder.routing.client;

import java.net.Inet4Address;

/**
 * Object which describes a route entry in the forwarding table.
 * Can be extended to include additional data.
 * @author Jaco
 * @version 09-03-2015
 */
public class AbstractRoute {
	public Inet4Address nextHop;
}
