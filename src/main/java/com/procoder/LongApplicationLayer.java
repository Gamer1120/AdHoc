package com.procoder;

/**
 * com.procoder.Application Layer for the Ad hoc multi-client chat application.
 *
 * @author Michael Koopman s1401335, Sven Konings s1534130, Wouter Timmermans
 *         s1004751, René Boschma s???
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.LinkedList;
import java.util.Queue;

import javafx.scene.image.Image;

import com.procoder.gui.AdhocGUI;
import com.procoder.transport.AdhocTransport;
import com.procoder.transport.HostList;
import com.procoder.transport.TimestampTransport;
import com.procoder.util.ArrayUtils;

// TODO Gaat stuk bij IPv6 adressen

@SuppressWarnings("restriction")
public class LongApplicationLayer implements AdhocApplication {

	private static final String ENCODING = "UTF-8";
	private HashMap<InetAddress, Queues> receivedPackets;

	private AdhocTransport transportLayer;
	private AdhocGUI gui;

	public enum PacketType {
		TEXT((byte) 0), IMAGE((byte) 1), AUDIO((byte) 2), FILE((byte)3), UNDEFINED((byte) 4);

		private byte number;


		PacketType(byte number) {
			this.number = number;
		}

		public byte toByte() {
			return number;
			}

		public static PacketType parseByte(byte b) {
			PacketType result = UNDEFINED;
			for(PacketType type : PacketType.values()) {
				if(type.number == b) {
					result = type;
					break;
				}
			}
			return result;
		}
	}



	/**
	 * Creates a new ApplicationLayer using a given GUI. Also starts the
	 * TransportLayer.
	 *
	 * @param gui
	 */
	public LongApplicationLayer(AdhocGUI gui) {
		this.gui = gui;
		this.receivedPackets = new HashMap<InetAddress, Queues>();
		this.transportLayer = new TimestampTransport(this);
	}

	// ---------------------------//
	// SENDING TO TRANSPORT LAYER //
	// ---------------------------//

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
	public void sendString(InetAddress dest, String input) {
		byte[] packet = null;
		try {
			packet = generatePacket(InetAddress.getLocalHost(), dest, PacketType.TEXT, input.getBytes(ENCODING));
		} catch (IOException e) {
			e.printStackTrace();
		}
		transportLayer.send(dest, packet);
	}

	@Override
	public void sendFile(InetAddress dest, File input) {
		Path path = Paths.get(input.getAbsolutePath());
		byte[] packet = null;
		try {
			packet = generatePacket(InetAddress.getLocalHost(), dest, PacketType.FILE, Files.readAllBytes(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		transportLayer.send(dest, packet);
	}

	@Override
	public void sendImage(InetAddress dest, File input) {
		Path path = Paths.get(input.getAbsolutePath());
		byte[] packet = null;
		try {
			packet = generatePacket(InetAddress.getLocalHost(), dest, PacketType.IMAGE, Files.readAllBytes(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		transportLayer.send(dest, packet);

	}

	@Override
	public void sendAudio(InetAddress dest, File input) {
		Path path = Paths.get(input.getAbsolutePath());
		byte[] packet = null;
		try {
			packet = generatePacket(InetAddress.getLocalHost(), dest, PacketType.FILE, Files.readAllBytes(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		transportLayer.send(dest, packet);
	}


	/**
	 *
	 * @param sender Afzender van dit bericht
	 * @param destination Geaddresseerde van dit bericht
	 * @param type Het type van de data
	 * @param data De uiteindelijke
	 * @return
	 */
	public byte[] generatePacket(InetAddress sender, InetAddress destination, PacketType type, byte[] data) {
		// TODO Hier localhost gebruiken / de methode van Sven
		byte [] sendBytes = sender.getAddress();
		byte [] destBytes = destination.getAddress();
		byte typeBytes = type.toByte();
		int messageSize = sendBytes.length + destBytes.length + 1 + data.length;
		ByteBuffer buf = ByteBuffer.allocate(messageSize + Long.BYTES);
		buf.putLong(messageSize);
		buf.put(typeBytes);
		buf.put(sendBytes);
		buf.put(destBytes);
		buf.put(data);

		return buf.array();

	}

	// ---------------//
	// SENDING TO GUI //
	// ---------------//

	class Queues {
		Queue<Byte> incoming;
		Queue<Byte> message;
		long remaining;

		public Queues() {
			this.incoming = new LinkedList<Byte>();
			this.message = new LinkedList<Byte>();
			this.remaining = 0;
		}
	}

	/**
	 * After determining which type of packet it is, it sends the data to the
	 * GUI.
	 *
	 * @param packet
	 *            The packet to be sent.
	 */
	@Override
	public void processPacket(DatagramPacket packet) {
		byte[] bytestream = packet.getData();
		InetAddress sender = packet.getAddress();
		Queues savedQueues = receivedPackets.get(sender); // Kan null zijn.
		savedQueues = savedQueues == null ? new Queues() : savedQueues;
		receivedPackets.put(sender, savedQueues);

		// Add all bytes from this message to incoming.
		for (byte b : bytestream) {
			savedQueues.incoming.add(b);
		}

		if (savedQueues.remaining == 0
				&& savedQueues.incoming.size() >= Long.BYTES) {

			// Get the length of the message
			ByteBuffer buf = ByteBuffer.wrap(ArrayUtils
					.toPrimitiveArray(savedQueues.incoming
							.toArray(new Byte[savedQueues.incoming.size()])));
			savedQueues.remaining = buf.getLong();

			// Remove the length of one long from the message
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
				// Stuur naar GUI en maak de message empty.
				byte[] message = ArrayUtils
						.toPrimitiveArray(savedQueues.message
								.toArray(new Byte[0]));

				PacketType type = PacketType.parseByte(message[0]);
				byte[] senderBytes = Arrays.copyOfRange(message, 1, 5);
				byte[] destenBytes = Arrays.copyOfRange(message, 5, 9);
				byte[] dataBytes = Arrays.copyOfRange(message, 9, message.length);
				switch (type) {
					case TEXT:
						gui.processString(parseIP(senderBytes), parseIP(destenBytes), getData(dataBytes));
						break;
					case IMAGE:
						ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
						gui.processImage(parseIP(senderBytes), parseIP(destenBytes), new Image(in));
						break;
					case AUDIO:
						String audioname = System.currentTimeMillis()+".audiofile";
						try{
							FileOutputStream aos =
									new FileOutputStream("receivedFile.file");
							aos.write(dataBytes);
							aos.close();}
						catch (IOException e){
							e.printStackTrace();
						}
						gui.processFile(parseIP(senderBytes), parseIP(destenBytes), new File(audioname));
						break;
					case FILE:
						String filename = System.currentTimeMillis()+".file";
						try{
							FileOutputStream aos =
									new FileOutputStream("receivedFile.file");
							aos.write(dataBytes);
							aos.close();}
						catch (IOException e){
							e.printStackTrace();
						}
						gui.processFile(parseIP(senderBytes), parseIP(destenBytes), new File(filename));
						break;
					case UNDEFINED:
						System.out
								.println("Just received a packet with an undefined type, namely "
										+ message[0]);
						break;
				}

				savedQueues.message = new LinkedList<>();
			}
		}
	}

	/**
	 * Gets the sender of the packet as String, required for the GUI. The sender
	 * is the 2nd to 5th byte in a packet.
	 *
	 * @param byteAddress
	 *            Said packet.
	 * @return The sender of the packet as String.
	 */
	public String parseIP(byte[] byteAddress) {
		try {
			return InetAddress.getByAddress(byteAddress).toString();
		} catch (UnknownHostException e) {
			System.out.println("[AL] [RCD] Kan de bytearray " + byteAddress + " niet omzetten naar een IP adres");
			return "";
		}
	}

	/**
	 * Returns the text that is in the packet as String, required for the GUI.
	 * The data is everything after the first 5 bytes in a packet.
	 *
	 * @param bytestream
	 *            Said packet.
	 * @return The text in that packet.
	 * @throws UnsupportedEncodingException
	 */
	public String getData(byte[] bytestream) {
		String data = "";
		try {
			data = new String(bytestream, ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return data;
	}

	// --------------- //
	// HELPFUL METHODS //
	// --------------- //

	@Override
	public HostList getKnownHostList() {
		return transportLayer.getKnownHostList();
	}

}