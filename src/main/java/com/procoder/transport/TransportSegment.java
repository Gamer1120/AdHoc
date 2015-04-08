package com.procoder.transport;

import java.nio.ByteBuffer;

import com.procoder.util.AirKont;

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
        byte[] primBytes = AirKont.toPrimitiveArray(data);

        ByteBuffer buf = ByteBuffer.allocate(data.length + 8);
        buf.putLong(timeStamp);
        buf.put(primBytes);
        buf.flip();

        return buf.array();

    }

    static TransportSegment parseNetworkData(byte[] data) {

        ByteBuffer buf = ByteBuffer.wrap(data);
        long timestamp = buf.getLong();

        byte[] actualData = new byte[buf.remaining()];

        for(int i = 0; buf.hasRemaining(); i++) {
            actualData[i] = buf.get();
        }

        return new TransportSegment(AirKont.toObjectArray(actualData), timestamp);



    }

}
