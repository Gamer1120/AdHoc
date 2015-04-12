package com.procoder.routing.client;

import com.procoder.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;

/**
 * Link layer interface, used for clarity
 * @author Jaco ter Braak, Twente University
 * @author Wouter Timmermans, Twente University
 * @version 12-04-2015
 */

public class LinkLayer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkLayer.class);

	private RoutingUDPLinkLayer client;

	public LinkLayer(RoutingUDPLinkLayer client) {
		this.client = client;
	}

	/**
	 * Gets the address within the network, associated with this interface
	 * @return address
	 */
	public Inet4Address getOwnAddress() {
		Inet4Address result = null;
		try {
			result = (Inet4Address) NetworkUtils.getLocalHost();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Gets the cost of the connected link
	 * @return The cost as a positive integer (or -1 if there is no link)
	 */
	public byte getLinkCost(Inet4Address destination) {
		byte result = -1;
		try {
			if (getOwnAddress().equals(destination)) {
				result = 0;
			} else if (destination.isReachable(100)) {
				result = 1;
			}
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
