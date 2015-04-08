package com.procoder;

import com.procoder.transport.Transport;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class NetworkLayer implements Network {
	// TODO routing tables
	private final static int LENGTH = 1500;
	private final static int PORT = 7777;
	private final static int TTL = 4;
	private Transport transportLayer;
	private InetAddress multicast;
	private MulticastSocket socket;

	public NetworkLayer(Transport transportLayer) {
		this.transportLayer = transportLayer;
		try {
			socket = new MulticastSocket(PORT);
			socket.setTimeToLive(TTL);
			multicast = InetAddress.getByName("228.0.0.0");
			NetworkInterface iface = NetworkInterface.getByName("wlan0");
			socket.joinGroup(new InetSocketAddress(multicast, PORT), iface);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(InetAddress dest, byte[] data) {
		// Create a packet and send it to the multicast address
		byte[] packetData = new byte[data.length + 1];
		packetData[0] = TTL;
		System.arraycopy(data, 0, packetData, 1, data.length);
		System.out.println("[NL] Sending: " + Arrays.toString(packetData));
		DatagramPacket packet = new DatagramPacket(packetData,
				packetData.length, multicast, PORT);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// Receive packets and forward them to the com.procoder.transport layer
		while (true) {
			DatagramPacket packet = new DatagramPacket(new byte[LENGTH], LENGTH);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			byte[] data = Arrays.copyOfRange(packet.getData(), 0,
					packet.getLength());
			System.out.println("[NL] Received: " + Arrays.toString(data));
			packet.setData(data, 0, data.length);
			if (--data[0] > 0) {
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			data = Arrays.copyOfRange(data, 1,
					data.length - 1);
			packet.setData(data);
			transportLayer.processPacket(packet);
		}
	}
}
