package com.procoder.transport;

import com.procoder.util.AirKont;

import java.nio.ByteBuffer;
import java.util.Arrays;

class TransportSegment {

    long timeStamp;
    Byte[] data;
    byte flags;

    TransportSegment(Byte[] data) {

        timeStamp = System.currentTimeMillis();
        this.data = data;
        //|SEQ 1bit|ACK 1bit|DISCOVERY 1bit|
        flags = 0;

    }

    private TransportSegment(Byte[] data, long timeStamp, byte flags) {
        this.timeStamp = timeStamp;
        this.data = data;
        this.flags = flags;
    }

    boolean isDiscover() {
        return (flags & 0B001) != 0;
    }

    void setDiscover() {
        flags = (byte) (flags | 0B001);
    }

    byte[] toByteArray() {
        byte[] primBytes = AirKont.toPrimitiveArray(data);

        ByteBuffer buf = ByteBuffer.allocate(data.length + Long.BYTES + 1);
        System.out.println("[TL] original data: " + Arrays.toString(primBytes));
        buf.putLong(timeStamp);
        buf.put(flags);
        buf.put(primBytes);
        buf.flip();

        System.out.println("[TL] data + timestamp: " + Arrays.toString(buf.array()));

        return buf.array();

    }

    static TransportSegment parseNetworkData(byte[] data) {

        ByteBuffer buf = ByteBuffer.wrap(data);
        long timestamp = buf.getLong();
        byte flags = buf.get();

        byte[] actualData = new byte[buf.remaining()];

        for(int i = 0; buf.hasRemaining(); i++) {
            actualData[i] = buf.get();
        }

        return new TransportSegment(AirKont.toObjectArray(actualData), timestamp, flags);



    }

}
