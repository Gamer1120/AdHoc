import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class NetworkLayer implements Network {
	// TODO routing tables
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
			socket.joinGroup(multicast);
		} catch (IOException e) {
			// TODO betere error handling
			e.printStackTrace();
		}
	}

	@Override
	public void send(InetAddress dest, byte[] data) {
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
		DatagramPacket packet = new DatagramPacket(null, 0);
		try {
			socket.receive(packet);
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
