package com.procoder;

import com.procoder.transport.HostList;

import java.io.File;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface AdhocApplication {

    public void sendString(InetAddress dest, String data);

    public void sendFile(InetAddress dest, File input);

	public void sendImage(InetAddress dest, File input);

	public void sendAudio(InetAddress dest, File input);

    public void processPacket(DatagramPacket data);

    public HostList getKnownHostList();

}
