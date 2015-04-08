package com.procoder.transport;

import com.procoder.util.AirKont;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
        System.out.println("[TL] original data: " + Arrays.toString(primBytes));
        buf.putLong(timeStamp);
        buf.put(primBytes);
        buf.flip();

        System.out.println("[TL] data + timestamp: " + Arrays.toString(buf.array()));

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
