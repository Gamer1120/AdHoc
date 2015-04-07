/**
 * Application Layer for the Ad hoc multi-client chat application.
 * 
 * @author Michael Koopman s1401335, Sven Konings s1534130, Wouter ??? s???, René Boschma s???
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ApplicationLayer implements Application {

	private Transport transportLayer;
	private GUI gui;

	/**
	 * Creates a new ApplicationLayer using a given GUI. Also starts the
	 * TransportLayer.
	 * 
	 * @param gui
	 */
	public ApplicationLayer(GUI gui) {
		this.gui = gui;
		// this.transportLayer = new TransportLayer(this);
		this.transportLayer = null;
	}

	/**
	 * Sends a packet to the Transport Layer.
	 * 
	 * @param dest
	 *            The final destination of this packet
	 * @param input
	 *            The Object that should be sent. This can be either text or a
	 *            file.
	 */
	@Override
	public void send(InetAddress dest, Object input) {
		if (input instanceof String) {
			// TODO: Add some bytes to determine whether the message is text or
			// a file.
			byte[] sender = null;
			try {
				sender = InetAddress.getLocalHost().getAddress();
				transportLayer.send(dest, generatePacket(null, null, null));
			} catch (UnknownHostException e) {
				System.out.println("Could not get localhost somehow.");
			}
		}
		/*
		 * else if (input instanceof File) { Path path = Paths.get(((File)
		 * input).getAbsolutePath()); TransportLayer.sendFile(dest,
		 * Files.readAllBytes(path)); }
		 */
	}

	/**
	 * Sends a packet to the GUI.
	 * 
	 * @param packet
	 *            The packet to be sent.
	 */
	@Override
	public void processPacket(DatagramPacket packet) {
		byte[] bytestream = packet.getData();
		// Breaks up the packet in a sender and the message.
		gui.sendString(getSender(bytestream), getData(bytestream));
	}

	public byte[] generatePacket(byte[] type, byte[] sender, byte[] data) {
		// byte[] retArray = merge(sender, ((String) input).getBytes());
		return null;
	}

	/**
	 * Gets the sender of the packet as String, required for the GUI. The sender
	 * is the first 4 bytes in a packet.
	 * 
	 * @param bytestream
	 *            Said packet.
	 * @return The sender of the packet as String.
	 */
	public String getSender(byte[] bytestream) {
		return bytestream[0] + "." + bytestream[1] + "." + bytestream[2] + "."
				+ bytestream[3];
	}

	/**
	 * Returns the text that is in the packet as String, required for the GUI.
	 * The data is everything after the first 4 bytes in a packet.
	 * 
	 * @param bytestream
	 *            Said packet.
	 * @return The text in that packet.
	 */
	public String getData(byte[] bytestream) {
		return Arrays.toString(Arrays.copyOfRange(bytestream, 4,
				bytestream.length - 1));
	}

	/**
	 * Merges two arrays into one.
	 * 
	 * @param first
	 * @param second
	 * @return The first and second array merged.
	 */
	public byte[] merge(byte[] first, byte[] second) {
		byte[] retByte = new byte[first.length + second.length];
		System.arraycopy(first, 0, retByte, 0, first.length);
		System.arraycopy(second, 0, retByte, first.length, second.length);
		return retByte;
	}

}
