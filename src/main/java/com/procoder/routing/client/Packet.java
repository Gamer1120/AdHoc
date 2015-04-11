package com.procoder.routing.client;

/**
 * Data packet consisting of source, destination and data
 * 
 * @author Jaco ter Braak, Twente University
 * @version 08-03-2015
 */
/*
 * 
 * DO NOT EDIT
 */
public class Packet {
	private int sourceAddress;
	private int destinationAddress;
	private DataTable data;

	/**
	 * Instantiates a new packet
	 * @param sourceAddress int
	 * @param destinationAddress int
	 * @param data a DataTable object. Can be a DataTable object with 0 columns, to represent no data.
	 */
	public Packet(int sourceAddress, int destinationAddress, DataTable data) {
		this.sourceAddress = sourceAddress;
		this.destinationAddress = destinationAddress;
		this.data = data;
	}

	public int getSourceAddress() {
		return sourceAddress;
	}

	public int getDestinationAddress() {
		return destinationAddress;
	}

	public DataTable getData() {
		return data;
	}

}
