package com.procoder.routing.client;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emulates a transport medium and handles the communication with the server.
 * This client should not be called directly from the protocol.
 * 
 * @author Jaco ter Braak, Twente University
 * @version 05-12-2013
 */
/*
 * 
 * DO NOT EDIT
 */
public class RoutingChallengeClient implements Runnable {
	private String protocolString = "ROUTINGCHALLENGE/2.0";

	private String host = "localhost";
	private int port = 8002;
	private int groupId;
	private String password;

	// thread for handling server messages
	private Thread eventLoopThread;

	private Socket socket;
	private InputStream inputStream;
	private String currentControlMessage = null;
	private String inputBuffer = "";

	private SimulationState simulationState = SimulationState.Idle;

	private IRoutingProtocol protocol;
	private int address;
	private Queue<Packet> packetBuffer = new LinkedList<Packet>();
	private HashMap<Integer, Integer> linkCosts = new HashMap<Integer, Integer>();

	public int getAddress() {
		return this.address;
	}

	public SimulationState getSimulationState() {
		return this.simulationState;
	}

	public void setRoutingProtocol(IRoutingProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * Constructs the client and connects to the server.
	 * 
	 * @param groupId
	 *            The group Id
	 * @param password
	 *            Password for the group
	 * @throws IOException
	 *             if the connection failed
	 */
	public RoutingChallengeClient(String serverAddress, int serverPort,
			int groupId, String password) throws IOException {
		this.groupId = groupId;
		this.password = password;
		this.host = serverAddress;
		this.port = serverPort;

		eventLoopThread = new Thread(this, "Event Loop Thread");

		Connect();
	}

	/**
	 * @return whether the simulation has been started
	 */
	public boolean IsSimulationRunning() {
		return (this.simulationState == SimulationState.Started
				|| this.simulationState == SimulationState.TestRunning || this.simulationState == SimulationState.TestComplete);
	}

	/**
	 * Connects to the challenge server
	 * 
	 * @throws IOException
	 *             if the connection failed
	 */
	public void Connect() throws IOException {
		try {
			socket = new Socket(host, port);
			socket.setTcpNoDelay(true);

			inputStream = socket.getInputStream();

			if (!getControlMessageBlocking().equals("REGISTER")) {
				throw new ProtocolException(
						"Did not get expected hello from server");
			}
			clearControlMessage();

			sendControlMessage("REGISTER " + this.groupId + " " + this.password);

			String reply = getControlMessageBlocking();
			if (!reply.equals("OK")) {
				String reason = reply.substring(reply.indexOf(' ') + 1);
				throw new ProtocolException("Could not register with server: "
						+ reason);
			}
			clearControlMessage();

			// start handling messages
			eventLoopThread.start();

		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Requests a simulation start from the server
	 */
	public void RequestStart() {
		if (this.simulationState == SimulationState.Idle) {
			sendControlMessage("START");
		}
	}

	/**
	 * Starts the simulation
	 */
	public void Start() {
		if (this.simulationState == SimulationState.Idle) {
			this.simulationState = SimulationState.Started;
		}
	}

	/**
	 * Stops the client, and disconnects it from the server.
	 */
	public void Stop() {
		this.simulationState = SimulationState.Finished;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles communication between the server and the protocol implementation
	 */
	public void run() {
		boolean stopThread = false;
		while (!stopThread && simulationState != SimulationState.Finished) {
			try {
				String message = getControlMessage();
				if (message != null) {
					if (message.startsWith("FAIL")) {
						if (message.split(" ").length > 1) {
							System.err
									.println("Failure: "
											+ message.substring(message
													.indexOf(' ') + 1));
						}
						clearControlMessage();
						Stop();
					} else if (message.startsWith("INFO")) {
						System.err
						.println("Info: "
								+ message.substring(message
										.indexOf(' ') + 1));
						clearControlMessage();
					} else if (message.startsWith("START")) {
						Start();
						clearControlMessage();
					} else if (message.startsWith("INITTEST")) {
						this.linkCosts.clear();
						this.packetBuffer.clear();
						this.simulationState = SimulationState.Started;
						clearControlMessage();
					} else if (message.startsWith("ADDRESS")) {
						if (message.split(" ").length > 1) {
							this.address = Integer.parseInt(message
									.split(" ")[1]);
						}
						clearControlMessage();
					} else if (message.startsWith("COST")) {
						if (message.split(" ").length == 3) {
							int iface = Integer.parseInt(message.split(" ")[1]);
							int cost = Integer.parseInt(message.split(" ")[2]);
							this.linkCosts.put(iface, cost);
							clearControlMessage();
						}
					} else if (message.startsWith("RUNTEST")) {
						this.simulationState = SimulationState.TestRunning;
						clearControlMessage();
					} else if (message.startsWith("TESTCOMPLETE")) {
						this.simulationState = SimulationState.TestComplete;
						clearControlMessage();
					} else if (message.startsWith("TABLE")) {
						if (message.split(" ").length > 1) {
							if (message.split(" ")[1].equals("REQUEST")) {
								UploadForwardingTable();
							} else if (message.split(" ")[1].equals("FAIL")) {
								System.err
										.println("Failed to upload forwarding table");
							}
						}
						clearControlMessage();
					} else if (message.startsWith("TRANSMIT")
							&& IsSimulationRunning()) {
						if (message.split(" ")[1].startsWith("DISCONNECTED")) {
							if (message.split(" ").length == 3) {
								// Disconnected interface
								int destination = Integer.parseInt(message
										.split(" ")[2]);
								clearControlMessage();
								linkCosts.put(destination, -1);
							}

						} else {
							if (message.split(" ").length == 2) {
								try {

									byte[] packetData = DatatypeConverter
											.parseBase64Binary(message
													.split(" ")[1]);

									// parse packet header
									ByteBuffer wrapped;

									wrapped = ByteBuffer.wrap(packetData, 0, 4);
									wrapped.order(ByteOrder.BIG_ENDIAN);
									int srcAddr = wrapped.getInt();

									wrapped = ByteBuffer.wrap(packetData, 4, 4);
									wrapped.order(ByteOrder.BIG_ENDIAN);
									int dstAddr = wrapped.getInt();

									wrapped = ByteBuffer.wrap(packetData, 8, 4);
									wrapped.order(ByteOrder.BIG_ENDIAN);
									int nRows = wrapped.getInt();

									wrapped = ByteBuffer
											.wrap(packetData, 12, 4);
									wrapped.order(ByteOrder.BIG_ENDIAN);
									int nColumns = wrapped.getInt();

									// parse packet data
									DataTable dataTable = new DataTable(
											nColumns);
									for (int i = 0; i < nRows; i++) {
										for (int j = 0; j < nColumns; j++) {
											wrapped = ByteBuffer.wrap(
													packetData, 16 + 4 * i
															* nColumns + 4 * j,
													4);
											wrapped.order(ByteOrder.BIG_ENDIAN);
											int cellData = wrapped.getInt();

											dataTable.set(i, j, cellData);
										}
									}

									// instantiate the packet with the provided
									// data
									Packet packet = new Packet(srcAddr,
											dstAddr, dataTable);

									if (!packetBuffer.offer(packet)) {
										System.err
												.println("Could not buffer packet.");
									}
								} catch (IllegalArgumentException
										| IndexOutOfBoundsException e) {
									System.err
											.println("Could not parse packet.");

									System.err.println(e.getMessage());

									e.printStackTrace();
								}

								clearControlMessage();
							}
						}
					} else if (message.startsWith("FINISH")
							|| message.startsWith("CLOSED")) {
						if (message.split(" ").length > 1) {
							System.err
									.println("Simulation aborted because: "
											+ message.substring(message
													.indexOf(' ') + 1));
						}
						System.out.println("Stopping...");
						Stop();
						this.simulationState = SimulationState.Finished;
						clearControlMessage();
					} else if (message.startsWith("NO-OP")) {
						clearControlMessage();
					}
				}
			} catch (ProtocolException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				stopThread = true;
			}
		}
	}

	private void UploadForwardingTable() {
		ConcurrentHashMap<Integer, ? extends AbstractRoute> table = this.protocol
				.getForwardingTable();

		sendControlMessage("TABLE "
				+ DatatypeConverter.printBase64Binary(SerializeTable(table)));
	}

	private byte[] SerializeTable(
			ConcurrentHashMap<Integer, ? extends AbstractRoute> table) {
		byte[] serialized = new byte[table.size() * 8];

		int i = 0;
		Iterator<Integer> it = table.keySet().iterator();
		while (it.hasNext()) {
			Integer destination = it.next();
			AbstractRoute route = table.get(destination);
			serialized[i + 0] = (byte) (destination >> 24);
			serialized[i + 1] = (byte) (destination >> 16);
			serialized[i + 2] = (byte) (destination >> 8);
			serialized[i + 3] = (byte) (destination >> 0);
			serialized[i + 4] = (byte) (route.nextHop >> 24);
			serialized[i + 5] = (byte) (route.nextHop >> 16);
			serialized[i + 6] = (byte) (route.nextHop >> 8);
			serialized[i + 7] = (byte) (route.nextHop >> 0);

			i += 8;
		}
		return serialized;
	}

	/**
	 * Gets the link cost for a certain interface
	 * 
	 * @param address
	 * @return the cost
	 */
	public int GetLinkCost(int address) {
		if (!IsSimulationRunning()) {
			return -1;
		}

		if (linkCosts.containsKey(address)) {
			return linkCosts.get(address);
		} else {
			return -1;
		}
	}

	/**
	 * Transmits a packet to another node
	 * 
	 * @param packet
	 *            Packet contains source address, destination address, and data.
	 *            Destination address may be 0 to transmit to all neighbours
	 *            (broadcast).
	 * @returns TransmissionResult (success or failure)
	 */
	public TransmissionResult Transmit(Packet packet) {
		if (!IsSimulationRunning()) {
			return TransmissionResult.Failure;
		}

		if (packet.getDestinationAddress() != 0
				&& (!linkCosts.containsKey(packet.getDestinationAddress()) || linkCosts
						.get(packet.getDestinationAddress()) == -1)) {
			return TransmissionResult.DestinationUnreachable;
		}

		// Serialize the table
		DataTable dataTable = packet.getData();

		byte[] packetData = new byte[16 + 4 * dataTable.getNRows()
				* dataTable.getNColumns()];

		int srcAddr = packet.getSourceAddress();
		packetData[0] = (byte) (srcAddr >> 24);
		packetData[1] = (byte) (srcAddr >> 16);
		packetData[2] = (byte) (srcAddr >> 8);
		packetData[3] = (byte) (srcAddr >> 0);

		int dstAddr = packet.getDestinationAddress();
		packetData[4] = (byte) (dstAddr >> 24);
		packetData[5] = (byte) (dstAddr >> 16);
		packetData[6] = (byte) (dstAddr >> 8);
		packetData[7] = (byte) (dstAddr >> 0);

		int nRows = packet.getData().getNRows();
		packetData[8] = (byte) (nRows >> 24);
		packetData[9] = (byte) (nRows >> 16);
		packetData[10] = (byte) (nRows >> 8);
		packetData[11] = (byte) (nRows >> 0);

		int nColumns = packet.getData().getNColumns();
		packetData[12] = (byte) (nColumns >> 24);
		packetData[13] = (byte) (nColumns >> 16);
		packetData[14] = (byte) (nColumns >> 8);
		packetData[15] = (byte) (nColumns >> 0);

		for (int i = 0; i < packet.getData().getNRows(); i++) {
			for (int j = 0; j < packet.getData().getNColumns(); j++) {

				int cellData = packet.getData().get(i, j);
				packetData[16 + i * packet.getData().getNColumns() * 4 + j * 4
						+ 0] = (byte) (cellData >> 24);
				packetData[16 + i * packet.getData().getNColumns() * 4 + j * 4
						+ 1] = (byte) (cellData >> 16);
				packetData[16 + i * packet.getData().getNColumns() * 4 + j * 4
						+ 2] = (byte) (cellData >> 8);
				packetData[16 + i * packet.getData().getNColumns() * 4 + j * 4
						+ 3] = (byte) (cellData >> 0);
			}
		}
		if (sendControlMessage("TRANSMIT "
				+ DatatypeConverter.printBase64Binary(packetData))) {
			return TransmissionResult.Success;
		} else {
			return TransmissionResult.Failure;
		}

	}

	/**
	 * Receives a packet from another node
	 */
	public Packet Receive() {
		if (!IsSimulationRunning()) {
			return null;
		}

		return packetBuffer.poll();
	}

	/**
	 * Waits for a control message from the server
	 * 
	 * @return the message
	 * @throws ProtocolException
	 *             if a corrupt message was received
	 */
	private String getControlMessageBlocking() throws ProtocolException {
		// Block while waiting for message
		String controlMessage = getControlMessage();
		while (controlMessage == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			controlMessage = getControlMessage();
		}

		return controlMessage;
	}

	/**
	 * Removes the first message from the queue Call this when you have
	 * processed a message
	 */
	private void clearControlMessage() {
		this.currentControlMessage = null;
	}

	/**
	 * Obtains a message from the server, if any exists.
	 * 
	 * @return the message, null if no message was present
	 * @throws ProtocolException
	 *             if there was a protocol mismatch
	 */
	private String getControlMessage() throws ProtocolException {
		try {
			if (this.currentControlMessage == null
					&& (inputStream.available() > 0)) {

				char currentChar = ' ';
				while (inputStream.available() > 0 && currentChar != '\n') {
					currentChar = (char) inputStream.read();
					inputBuffer += currentChar;
				}

				if (inputBuffer.endsWith("\n")) {
					String line = inputBuffer.substring(0,
							inputBuffer.length() - 1);
					inputBuffer = "";

					// System.out.println(line);

					if (line.startsWith(protocolString)) {
						this.currentControlMessage = line
								.substring(protocolString.length() + 1);
					} else {
						throw new ProtocolException(
								"Protocol mismatch with server");
					}
				}
			}
		} catch (IOException e) {
		}
		return this.currentControlMessage;
	}

	/**
	 * Sends a message to the server
	 * 
	 * @param message
	 */
	private boolean sendControlMessage(String message) {
		try {
			socket.getOutputStream().write(
					(protocolString + " " + message + "\n").getBytes());
			socket.getOutputStream().flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public enum SimulationState {
		Idle, Started, TestRunning, TestComplete, Finished
	}
}
