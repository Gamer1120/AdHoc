package com.procoder;

/**
 * Long Application Layer for the Ad hoc multi-client chat application.
 *
 * @author Michael Koopman s1401335, Sven Konings s1534130, Wouter Timmermans
 *         s1004751, Rene Boschma s1581899
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.procoder.gui.Main;
import com.procoder.transport.AdhocTransport;
import com.procoder.transport.HostList;
import com.procoder.transport.TCPLikeTransport;
import com.procoder.util.ArrayUtils;
import com.procoder.util.NetworkUtils;

@SuppressWarnings("restriction")
public class LongApplicationLayer implements AdhocApplication {

    private static final String ENCODING = "UTF-8";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(LongApplicationLayer.class);

    private HashMap<InetAddress, Queues> receivedPackets;

    private AdhocTransport transportLayer;
    private Main gui;

    /**
     * Creates a new ApplicationLayer using a given GUI. Also starts the
     * TransportLayer.
     *
     * @param gui
     */
    public LongApplicationLayer(Main gui) {
        this.gui = gui;
        this.receivedPackets = new HashMap<InetAddress, Queues>();
        this.transportLayer = new TCPLikeTransport(this);
    }

    // ---------------------------//
    // SENDING TO TRANSPORT LAYER //
    // ---------------------------//

    /**
     * Sends a String as a bytearray to the Transport Layer.
     *
     * @param dest
     *            The final destination of this packet.
     * @param input
     *            The String that should be sent.
     */
    @Override
    public void sendString(InetAddress dest, String input) {
        byte[] packet = null;
        try {
            packet = generatePacket(dest, PacketType.TEXT,
                    input.getBytes(ENCODING), new byte[] {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        transportLayer.send(dest, packet);
    }

    /**
     * Sends a File as a bytearray to the Transport Layer.
     *
     * @param dest
     *            The final destination of this packet.
     * @param input
     *            The File that should be sent.
     */
    @Override
    public void sendFile(InetAddress dest, File input) {
        sendPacket(dest, input, PacketType.FILE);
    }

    /**
     * Sends an image as a bytearray to the Transport Layer.
     *
     * @param dest
     *            The final destination of this packet.
     * @param input
     *            The image that should be sent.
     */
    @Override
    public void sendImage(InetAddress dest, File input) {
        sendPacket(dest, input, PacketType.IMAGE);
    }

    /**
     * Sends an audio file as a bytearray to the Transport Layer.
     *
     * @param dest
     *            The final destination of this packet.
     * @param input
     *            The image that should be sent.
     */
    @Override
    public void sendAudio(InetAddress dest, File input) {
        sendPacket(dest, input, PacketType.AUDIO);
    }

    /**
     * Sends a File to the Transport Layer using the given destination, the File
     * and the type the File is.
     * 
     * @param dest
     *            The final destination of this packet.
     * @param input
     *            The File to send.
     * @param type
     *            The PacketType of this packet. Can either be TEXT, IMAGE,
     *            AUDIO or FILE.
     */
    public void sendPacket(InetAddress dest, File input, PacketType type) {
        Path path = Paths.get(input.getAbsolutePath());
        String filename = input.getName();
        if (filename.length() > Byte.MAX_VALUE - Byte.MIN_VALUE) {
            filename = System.currentTimeMillis()
                    + filename.substring(filename.lastIndexOf('.') + 1);
        }
        byte[] packet = null;
        try {
            packet = generatePacket(dest, type, Files.readAllBytes(path),
                    filename.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        transportLayer.send(dest, packet);
    }

    /**
     * Generates a packet to send to the Transport Layer.
     * 
     * @param destination
     *            The final destination of this packet.
     * @param type
     *            The type of this packet.
     * @param data
     *            The bytearray that should be sent.
     * @param filename
     *            The filename as bytearray.
     * @return
     */
    public byte[] generatePacket(InetAddress destination, PacketType type,
            byte[] data, byte[] filename) {
        byte[] sendBytes = new byte[4];
        sendBytes = NetworkUtils.getLocalHost().getAddress();
        byte[] destBytes = destination.getAddress();
        byte typeBytes = type.toByte();
        int messageSize = sendBytes.length + destBytes.length + 2
                + filename.length + data.length;
        ByteBuffer buf = ByteBuffer.allocate(messageSize + Long.BYTES);
        buf.putLong(messageSize);
        buf.put(typeBytes);
        buf.put((byte) (filename.length + Byte.MIN_VALUE));
        buf.put(filename);
        buf.put(sendBytes);
        buf.put(destBytes);
        buf.put(data);

        return buf.array();
    }

    // ---------------//
    // SENDING TO GUI //
    // ---------------//

    /**
     * After receiving a packet from the Transport Layer, it checks whether it's
     * a full packet. If yes, send it to the GUI. If no, put it in a Queue and
     * don't send it until the full packet has been received.
     *
     * @param packet
     *            The packet that was received.
     */
    @Override
    public void processPacket(DatagramPacket packet) {
        byte[] bytestream = packet.getData();
        InetAddress sender = packet.getAddress();
        Queues savedQueues = receivedPackets.get(sender);
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
                int filenameLength = (int) message[1] - Byte.MIN_VALUE;
                byte[] fn = Arrays.copyOfRange(message, 2, 2 + filenameLength);
                byte[] senderBytes = Arrays.copyOfRange(message,
                        2 + filenameLength, 6 + filenameLength);
                byte[] destenBytes = Arrays.copyOfRange(message,
                        6 + filenameLength, 10 + filenameLength);
                byte[] dataBytes = Arrays.copyOfRange(message,
                        10 + filenameLength, message.length);
                switch (type) {
                case TEXT:
                    gui.processString(parseIP(senderBytes),
                            parseIP(destenBytes), getData(dataBytes));
                    break;
                case IMAGE:
                    ByteArrayInputStream in = new ByteArrayInputStream(
                            dataBytes);
                    gui.processImage(parseIP(senderBytes),
                            parseIP(destenBytes), new Image(in));
                    break;
                case AUDIO:
                    String audioname = new String(fn);
                    try {
                        FileOutputStream aos = new FileOutputStream(audioname);
                        aos.write(dataBytes);
                        aos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    gui.processAudio(parseIP(senderBytes),
                            parseIP(destenBytes), new File(audioname));
                    break;
                case FILE:
                    String filename = new String(fn);
                    try {
                        FileOutputStream aos = new FileOutputStream(filename);
                        aos.write(dataBytes);
                        aos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    gui.processFile(parseIP(senderBytes), parseIP(destenBytes),
                            new File(filename));
                    break;
                case UNDEFINED:
                    LOGGER.error(
                            "Just received a packet with an undefined type, namely {}",
                            message[0]);
                    break;
                }

                savedQueues.message = new LinkedList<>();
            }
        }
    }

    // --------------- //
    // HELPFUL METHODS //
    // --------------- //

    /**
     * Gets the IP of the sender of a packet as String, required for the GUI.
     * 
     * @param byteAddress
     *            Said packet.
     * @return The sender of the packet as String.
     */
    public String parseIP(byte[] byteAddress) {
        try {
            return InetAddress.getByAddress(byteAddress).getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.error(
                    "[AL] [RCD] Kan de bytearray {} niet omzetten naar een IP adres",
                    byteAddress);
            return "";
        }
    }

    /**
     * Returns the text that is in the packet as String, required for the GUI.
     *
     * @param bytestream
     *            Said packet.
     * @return The text in that packet.
     * @throws UnsupportedEncodingException
     *             If UTF-8 stops existing somehow.
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

    /**
     * Returns the known host list.
     * 
     * @return The known host list.
     */
    @Override
    public HostList getKnownHostList() {
        return transportLayer.getKnownHostList();
    }

    /**
     * Enum class for PacketType.
     */
    public enum PacketType {
        TEXT((byte) 0), IMAGE((byte) 1), AUDIO((byte) 2), FILE((byte) 3), UNDEFINED(
                (byte) 4);

        private byte number;

        PacketType(byte number) {
            this.number = number;
        }

        /**
         * Returns the PacketType a byte is.
         * 
         * @param b
         *            The byte.
         * @return The PacketType the byte is.
         */
        public static PacketType parseByte(byte b) {
            PacketType result = UNDEFINED;
            for (PacketType type : PacketType.values()) {
                if (type.number == b) {
                    result = type;
                    break;
                }
            }
            return result;
        }

        /**
         * Returns the byte a PacketType is.
         * 
         * @return The byte the PacketType is.
         */
        public byte toByte() {
            return number;
        }
    }

    /**
     * Enum class for Queues. This class is used to map multiple Queues to one
     * InetAddress.
     */
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

}
