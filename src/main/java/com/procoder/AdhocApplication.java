package com.procoder;

import java.net.DatagramPacket;
import java.net.InetAddress;

import com.procoder.transport.HostList;

public interface AdhocApplication {

	public void send(InetAddress dest, String data);

	public void send(InetAddress dest, File input);

	public void processPacket(DatagramPacket data);

	public HostList getKnownHostList();

}
