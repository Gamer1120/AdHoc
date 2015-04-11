package com.procoder.routing.client;

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
	private RoutingChallengeClient client;

	public LinkLayer(RoutingChallengeClient client) {
		this.client = client;
	}

	/**
	 * Gets the address within the network, associated with this interface
	 * @return address
	 */
	public int getOwnAddress() {
		return client.getAddress();
	}

	/**
	 * Gets the cost of the connected link
	 * @return The cost as a positive integer (or -1 if there is no link)
	 */
	public int getLinkCost(int destination) {
		return client.GetLinkCost(destination);
	}

	/**
	 * Transmits a packet
	 * @param packet
	 * @return the result of the transmission
	 */
	public TransmissionResult transmit(Packet packet) {
		return (client.Transmit(packet));
	}

	/**
	 * Receives a packet (if any)
	 * @return the packet (or null if no packet is available)
	 */
	public Packet receive() {
		return client.Receive();
	}
}
