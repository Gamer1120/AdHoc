package com.procoder;

import java.io.File;
import java.net.DatagramPacket;
import java.net.InetAddress;

import com.procoder.transport.HostList;

public interface AdhocApplication {
	
	// ---------------------------//
	// SENDING TO TRANSPORT LAYER //
	// ---------------------------//
	public void sendText(InetAddress dest, String data);

	public void sendImage(InetAddress dest, File input);

	public void sendMusic(InetAddress dest, File input);

	public void sendFile(InetAddress dest, File input);

	// ---------------//
	// SENDING TO GUI //
	// ---------------//

	public void processPacket(DatagramPacket data);

	// --------------- //
	// HELPFUL METHODS //
	// --------------- //

	public HostList getKnownHostList();

}
