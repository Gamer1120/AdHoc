package com.procoder;

/**
 * Application Layer for the Ad hoc multi-client chat application.
 * 
 * @author Michael Koopman s1401335, Sven Konings s1534130, Wouter ??? s???, René Boschma s???
 */
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import com.procoder.gui.Main;
import com.procoder.transport.HostList;
import com.procoder.transport.TimestampTransport;
import com.procoder.transport.Transport;

public class FlagApplicationLayer implements AdhocApplication {

	private static final String ENCODING = "UTF-8";
	private static final int BEGIN = 0;
	private static final int END = -64;
	private HashMap<InetAddress, byte[]> receivedPackets;

	private Transport transportLayer;
	private Main gui;

	private enum PacketType {
		UNDEFINED, TEXT, FILE
	}

	/**
	 * Creates a new com.procoder.ApplicationLayer using a given
	 * com.procoder.GUI. Also starts the TransportLayer.
	 * 
	 * @param gui
	 */

	public FlagApplicationLayer(Main gui) {
		this.gui = gui;
		this.receivedPackets = new HashMap<InetAddress, byte[]>();
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
			if (dest != null) {
				sender = dest.getAddress();
			} else {
				sender = InetAddress.getLocalHost().getAddress();
			}
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
		System.out.println("[AL] [SND]: " + Arrays.toString(packet));
		if (input instanceof String) {
			transportLayer.send(dest, packet);

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
		return merge(
				new byte[] { BEGIN, BEGIN, BEGIN, BEGIN },
				merge(type,
						merge(sender,
								merge(data, new byte[] { END, END, END, END }))));
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
	public void processPacket(DatagramPacket packet) {
		byte[] bytestream = packet.getData();
		if (bytestream.length > 0) {
			System.out.println("[AL] [RCD]: " + Arrays.toString(bytestream));
			InetAddress sender = packet.getAddress();
			if (Arrays.equals(Arrays.copyOfRange(bytestream, 0, 4), new byte[] {
					BEGIN, BEGIN, BEGIN, BEGIN })) {
				if (Arrays.equals(Arrays.copyOfRange(bytestream,
						bytestream.length - 4, bytestream.length), new byte[] {
						END, END, END, END })) {
					gui.processString(getSender(bytestream), getData(bytestream));
				} else {
					receivedPackets.put(sender, bytestream);
				}
			} else if (Arrays.equals(Arrays.copyOfRange(bytestream,
					bytestream.length - 4, bytestream.length), new byte[] {
					END, END, END, END })) {
				byte[] fullPacket = merge(receivedPackets.get(sender),
						bytestream);
				gui.processString(getSender(fullPacket), getData(fullPacket));
			} else {
				byte[] fullPacket = merge(receivedPackets.get(sender),
						bytestream);
				receivedPackets.put(sender, fullPacket);
			}
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
		int packetByte = bytestream[4];
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
		return bytestream[5] + "." + bytestream[6] + "." + bytestream[7] + "."
				+ bytestream[8];
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
			dinges = new String(Arrays.copyOfRange(bytestream, 9,
					bytestream.length - 4), ENCODING);
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
	
	public HostList getKnownHostList(){
		return transportLayer.getKnownHostList();
	}

}
