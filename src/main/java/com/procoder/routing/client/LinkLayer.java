package com.procoder.routing.client;

import com.procoder.NetworkLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;

/**
 * Link layer interface, used for clarity
 * @author Jaco ter Braak, Twente University
 * @version 17-12-2013
 */
/*
 * 
 * DO NOT EDIT
 * 
 */
public class LinkLayer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkLayer.class);

	private RoutingClient client;

	public LinkLayer(RoutingClient client) {
		this.client = client;
	}

	/**
	 * Gets the address within the network, associated with this interface
	 * @return address
	 */
	public Inet4Address getOwnAddress() {
		try {
			return (Inet4Address) NetworkLayer.getLocalHost();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return null;
		}
	}

	public Inet4Address getBroadcastAddress() {
		try {
			Inet4Address broad = (Inet4Address) Inet4Address.getByName("228.0.0.0");
			return broad;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return null;
		}
	}

	/**
	 * Gets the cost of the connected link
	 * @return The cost as a positive integer (or -1 if there is no link)
	 */
	public byte getLinkCost(Inet4Address destination) {
		byte result = -1;
		try {
			destination.isReachable(100);
			result = 1;
		} catch (IOException e) {
			LOGGER.debug("Unable to ping {}", destination.getHostAddress());
		}

		return result;


	}

	/**
	 * Transmits a packet
	 * @param packet
	 * @return the result of the transmission
	 */
	public void transmit(Packet packet) {
		client.transmit(packet);
	}

	/**
	 * Receives a packet (if any)
	 * @return the packet (or null if no packet is available)
	 */
	public Packet receive() {
		return client.receive();
	}
}
