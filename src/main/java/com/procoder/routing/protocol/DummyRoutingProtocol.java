package com.procoder.routing.protocol;

import com.procoder.routing.client.*;

import java.net.Inet4Address;
import java.util.concurrent.ConcurrentHashMap;

public class DummyRoutingProtocol implements IRoutingProtocol {
	private LinkLayer linkLayer;
	private ConcurrentHashMap<Inet4Address, BasicRoute> forwardingTable = new ConcurrentHashMap<>();

	@Override
	public void init(LinkLayer linkLayer) {
		this.linkLayer = linkLayer;
		
		// First, send a broadcast packet (to address 0), with no data
		Packet discoveryBroadcastPacket = new Packet(this.linkLayer.getOwnAddress(), this.linkLayer.getBroadcastAddress(), new DVTable());
		this.linkLayer.transmit(discoveryBroadcastPacket);
	}

	@Override
	public void run() {
		try {
			while (true) {
				// Try to receive a packet
				Packet packet = this.linkLayer.receive();
				if (packet != null) {
					// Do something with the packet
				}
				
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			// We were interrupted, stop execution of the protocol
		}
	}

	@Override
	public ConcurrentHashMap<Inet4Address, BasicRoute> getForwardingTable() {
		return this.forwardingTable;
	}

	@Override
	public void stop() {

	}
}
