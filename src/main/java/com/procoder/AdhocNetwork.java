package com.procoder;

import java.net.InetAddress;

public interface AdhocNetwork extends Runnable {

    public void send(byte[] data);

    public void send(InetAddress dest, byte[] data);

}
