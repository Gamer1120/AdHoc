package com.procoder.routing.client;

import java.net.Inet4Address;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Describes the interface used for routing protocols
 * 
 * @author Jaco ter Braak, Twente University
 * @version 08-03-2015
 */
/*
 * 
 * DO NOT EDIT
 */
public interface IRoutingProtocol extends Runnable {

	/**
	 * This method is called by the framework before the simulation starts. The
	 * protocol implementation should be initialized here.
	 * 
	 * @param linkLayer
	 */
	void init(LinkLayer linkLayer);

	/**
	 * The method is called by the framework  during and after the simulation, to
	 * retrieve the local forwarding table.
	 * 
	 * @return ForwardingTable
	 */
	ConcurrentHashMap<Inet4Address, ? extends AbstractRoute> getForwardingTable();

	void stop();
}
