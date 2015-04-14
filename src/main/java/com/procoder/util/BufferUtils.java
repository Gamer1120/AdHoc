package com.procoder.util;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class BufferUtils {

    public static Inet4Address readI4Address(ByteBuffer buf) throws UnknownHostException {
        byte[] address = new byte[4];

        for(int i = 0; i < address.length; i++) {
            address[i] = buf.get();
        }

        return (Inet4Address) Inet4Address.getByAddress(address);
    }

}
