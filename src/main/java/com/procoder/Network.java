package com.procoder;

import java.net.InetAddress;

public interface Network extends Runnable {

    public void send(InetAddress dest, byte[] data);

}
