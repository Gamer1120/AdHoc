package com.procoder.transport;

import com.procoder.util.AirKont;

import java.nio.ByteBuffer;

class TransportSegment {

    long timeStamp;
    Byte[] data;

    TransportSegment(Byte[] data) {

        timeStamp = System.currentTimeMillis();
        this.data = data;

    }

    private TransportSegment(Byte[] data, long timeStamp) {
        this.timeStamp = timeStamp;
        this.data = data;
    }

    byte[] toByteArray() {
        byte[] primBytes = new byte[data.length];
        int i = 0;
        for (Byte b : data) {
            primBytes[i] = b;
        }

        ByteBuffer buf = ByteBuffer.allocate(data.length + Long.SIZE / 8);
        buf.putLong(timeStamp);
        buf.put(primBytes);

        return buf.array();

    }

    static TransportSegment parseNetworkData(byte[] data) {

        ByteBuffer buf = ByteBuffer.wrap(data);
        long timestamp = buf.getLong();

        return new TransportSegment(AirKont.toObjectArray(buf.array()), timestamp);



    }

}
