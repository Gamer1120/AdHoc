package com.procoder;

import java.net.DatagramPacket;
import java.net.InetAddress;

public interface AdhocApplication {

	public void send(InetAddress dest, Object data);

	public void processPacket(DatagramPacket data);

}