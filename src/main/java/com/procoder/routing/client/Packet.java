package com.procoder.routing.client;

import com.procoder.util.BufferUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Data packet consisting of source, destination and data
 * 
 * @author Jaco ter Braak, Twente University
 * @author Wouter Timmermans, Twente University
 * @version 11-04-2015
**/

public class Packet {
	private Inet4Address sourceAddress;
	private Inet4Address destinationAddress;
	private DVTable data;

	/**
	 * Instantiates a new packet
	 * @param sourceAddress int
	 * @param destinationAddress int
	 * @param data a DataTable object. Can be a DataTable object with 0 columns, to represent no data.
	 */
	public Packet(Inet4Address sourceAddress, Inet4Address destinationAddress, DVTable data) {
		this.sourceAddress = sourceAddress;
		this.destinationAddress = destinationAddress;
		this.data = data;
	}

	public Inet4Address getSourceAddress() {
		return sourceAddress;
	}

	public Inet4Address getDestinationAddress() {
		return destinationAddress;
	}

	public DVTable getData() {
		return data;
	}

	public byte[] toByteArray() {
		byte[] sourceBytes = sourceAddress.getAddress();
		byte[] destinBytes = destinationAddress.getAddress();
		byte[] tableBytes = data.toByteArray();

		ByteBuffer buf = ByteBuffer.allocate(sourceBytes.length + destinBytes.length + tableBytes.length);
		buf.put(sourceBytes);
		buf.put(destinBytes);
		buf.put(tableBytes);

		return buf.array();

	}

	public static Packet parseBytes(byte[] bytePacket) {
		Packet result = null;
		ByteBuffer buf = ByteBuffer.wrap(bytePacket);

		try {
			Inet4Address sourceAddress = BufferUtils.readI4Address(buf);
			Inet4Address destinAddress = BufferUtils.readI4Address(buf);
			DVTable table = DVTable.parseBytes(buf.array());
			result = new Packet(sourceAddress, destinAddress, table);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return result;


	}



}
