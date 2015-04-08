package com.procoder;

/**
 * com.procoder.Application Layer for the Ad hoc multi-client chat application.
 * 
 * @author Michael Koopman s1401335, Sven Konings s1534130, Wouter ??? s???, René Boschma s???
 */
import com.procoder.transport.TimestampTransport;
import com.procoder.transport.Transport;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ApplicationLayer implements Application {

	private static final String ENCODING = "UTF-8";

	private Transport transportLayer;
	private GUI gui;

	private enum PacketType {
		UNDEFINED, TEXT, FILE
	}

	/**
	 * Creates a new com.procoder.ApplicationLayer using a given
	 * com.procoder.GUI. Also starts the TransportLayer.
	 * 
	 * @param gui
	 */
	public ApplicationLayer(GUI gui) {
		this.gui = gui;
		this.transportLayer = new TimestampTransport(this);
	}

	// ---------------------------//
	// SENDING TO TRANSPORT LAYER //
	// ---------------------------//

	/**
	 * Sends a packet to the com.procoder.transport.Transport Layer.
	 * 
	 * @param dest
	 *            The final destination of this packet
	 * @param input
	 *            The Object that should be sent. This can be either text or a
	 *            file.
	 */
	@Override
	public void send(InetAddress dest, Object input) {
		byte[] sender = null;
		try {
			sender = InetAddress.getLocalHost().getAddress();
		} catch (UnknownHostException e) {
			System.out.println("Could not get localhost somehow.");
		}
		byte[] packet = null;
		try {
			packet = generatePacket(new byte[] { 0 }, sender,
					((String) input).getBytes(ENCODING));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		if (input instanceof String) {
			transportLayer.send(dest, packet);
			System.out.println("[AL] Sending: " + Arrays.toString(packet));

		} else if (input instanceof File) {
			Path path = Paths.get(((File) input).getAbsolutePath());
			try {
				transportLayer.send(
						dest,
						generatePacket(new byte[] { 1 }, sender,
								Files.readAllBytes(path)));
			} catch (IOException e) {
				System.out.println("Couldn't read file.");
			}
		}

	}

	/**
	 * Generates a packet with the given type, sender and data.
	 * 
	 * @param type
	 *            0 for text, 1 for File.
	 * @param sender
	 * @param data
	 * @return A packet with all these combined.
	 */
	public byte[] generatePacket(byte[] type, byte[] sender, byte[] data) {
		return merge(type, merge(sender, data));
	}

	// ---------------//
	// SENDING TO GUI //
	// ---------------//

	/**
	 * After determining which type of packet it is, it sends the data to the
	 * GUI.
	 * 
	 * @param bytestream
	 *            The packet to be sent.
	 */
	@Override
	public void processPacket(byte[] bytestream) {
		System.out.println("[AL] Received: " + Arrays.toString(bytestream));
		PacketType type = getType(bytestream);
		switch (type) {
		case TEXT:
			gui.sendString(getSender(bytestream), getData(bytestream));
			break;
		case FILE:
			gui.sendString(
					getSender(bytestream),
					"René, dinges stuurde net een bestand. Ik kan hem niet doorsturen. FIX JE SHIT D:");
			break;
		case UNDEFINED:
			gui.sendString(getSender(bytestream),
					"Received a packet from this source with an unknown type, namely: "
							+ bytestream[0]);
			break;
		}
	}

	/**
	 * Returns what type this packet is (for example text or a File.)
	 * 
	 * @param bytestream
	 *            Said packet.
	 * @return What type this packet is.
	 */
	public PacketType getType(byte[] bytestream) {
		int packetByte = bytestream[0];
		if (packetByte == 0) {
			return PacketType.TEXT;
		} else if (packetByte == 1) {
			return PacketType.FILE;
		} else {
			return PacketType.UNDEFINED;
		}
	}

	/**
	 * Gets the sender of the packet as String, required for the
	 * com.procoder.GUI. The sender is the 2nd to 5th byte in a packet.
	 * 
	 * @param bytestream
	 *            Said packet.
	 * @return The sender of the packet as String.
	 */
	public String getSender(byte[] bytestream) {
		return bytestream[1] + "." + bytestream[2] + "." + bytestream[3] + "."
				+ bytestream[4];
	}

	/**
	 * Returns the text that is in the packet as String, required for the
	 * com.procoder.GUI. The data is everything after the first 5 bytes in a
	 * packet.
	 * 
	 * @param bytestream
	 *            Said packet.
	 * @return The text in that packet.
	 * @throws UnsupportedEncodingException
	 */
	public String getData(byte[] bytestream) {
		String dinges = "";
		try {
			dinges = new String(Arrays.copyOfRange(bytestream, 5,
					bytestream.length), ENCODING);
		} catch (UnsupportedEncodingException e) {
			System.out.println(ENCODING
					+ " is not supported on this system. CRASHING...");
			e.printStackTrace();
		}
		return dinges;
	}

	// --------------- //
	// HELPFUL METHODS //
	// --------------- //

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
