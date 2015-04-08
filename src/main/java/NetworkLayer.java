import java.io.IOException;
import java.net.*;

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
			// TODO betere error handling
			e.printStackTrace();
		}
	}

	@Override
	public void send(InetAddress dest, byte[] data) {
		System.out.println("[NL] Sending a message!");
		// Create a packet and send it to the multicast address
		byte[] packetData = new byte[data.length + 1];
		packetData[0] = TTL;
		System.arraycopy(data, 0, packetData, 1, data.length);
		DatagramPacket packet = new DatagramPacket(data, data.length,
				multicast, PORT);
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO betere error handling
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// Receive packets and forward them to the transport layer
		while (true) {
			DatagramPacket packet = new DatagramPacket(new byte[LENGTH], LENGTH);
			try {
				socket.receive(packet);
				System.out.println("[NL] Received a message!");
			} catch (IOException e) {
				// TODO betere error handling
				e.printStackTrace();
			}
			byte[] data = packet.getData();
			if (--data[0] > 0) {
				try {
					socket.send(packet);
				} catch (IOException e) {
					// TODO betere error handling
					e.printStackTrace();
				}
			}
			packet.setData(data, 1, data.length - 1);
			transportLayer.processPacket(packet);
		}
	}
}
