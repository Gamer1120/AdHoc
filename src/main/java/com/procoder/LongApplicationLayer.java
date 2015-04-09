package com.procoder;

/**
 * com.procoder.Application Layer for the Ad hoc multi-client chat application.
 * 
 * @author Michael Koopman s1401335, Sven Konings s1534130, Wouter ??? s???, René Boschma s???
 */
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;

import com.procoder.transport.HostList;
import com.procoder.transport.TimestampTransport;
import com.procoder.transport.Transport;

import com.procoder.util.AirKont;

public class LongApplicationLayer implements Application {

	private static final String ENCODING = "UTF-8";
	private static final int BEGIN = 0;
	private static final int END = -64;
	private HashMap<InetAddress, Queues> receivedPackets;

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
	public LongApplicationLayer(GUI gui) {
		this.gui = gui;
		this.receivedPackets = new HashMap<InetAddress, Queues>();
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

	class Queues {
		Queue<Byte> incoming;
		Queue<Byte> message;
		long remaining;
	}

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
		InetAddress sender = packet.getAddress();
		Queues savedQueues = receivedPackets.get(sender); // Kan null zijn.
		// Add all bytes from this message to incoming.
		for (byte b : bytestream) {
			savedQueues.incoming.add(b);
		}
		if (savedQueues.remaining == 0
				&& savedQueues.incoming.size() >= Long.BYTES) {
			// Get the length of the message
			ByteBuffer buf = ByteBuffer.wrap(AirKont
					.toPrimitiveArray(savedQueues.incoming
							.toArray(new Byte[savedQueues.incoming.size()])));
			savedQueues.remaining = buf.getLong();
			// Remove the length from the message
			for (int i = 0; i < Long.BYTES; i++) {
				savedQueues.incoming.remove();
			}
		}
		if (savedQueues.remaining != 0) {
			while (savedQueues.remaining != 0
					&& savedQueues.incoming.size() > 0) {
				savedQueues.message.add(savedQueues.incoming.poll());
				savedQueues.remaining--;
			}

			if (savedQueues.remaining == 0) {
				//Stuur naar GUI en maak de message empty.
			}
		}
	}

	public long getLength(byte[] bytestream) {
		return ((bytestream[0] & 0xFFL) << 56)
				| ((bytestream[1] & 0xFFL) << 48)
				| ((bytestream[2] & 0xFFL) << 40)
				| ((bytestream[3] & 0xFFL) << 32)
				| ((bytestream[4] & 0xFFL) << 24)
				| ((bytestream[5] & 0xFFL) << 16)
				| ((bytestream[6] & 0xFFL) << 8)
				| ((bytestream[7] & 0xFFL) << 0);
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

	public HostList getKnownHostList() {
		return transportLayer.getKnownHostList();
	}

}