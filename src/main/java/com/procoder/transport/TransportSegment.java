package com.procoder.transport;

import com.procoder.util.AirKont;

import java.nio.ByteBuffer;
import java.util.Arrays;

class TransportSegment {

    int seq;
    int ack;
    Byte[] data;
    byte flags;

    TransportSegment(Byte[] data) {
        this.data = data;
        //|SEQ 1bit|ACK 1bit|DISCOVERY 1bit|
        flags = 0;

    }

    private TransportSegment(Byte[] data, byte flags, int seq, int ack) {
        this.flags = flags;
        this.seq = seq;
        this.ack = ack;
        this.data = data;
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
        buf.put(flags);
        buf.putInt(seq);
        buf.putInt(ack);
        buf.put(primBytes);
        buf.flip();

        System.out.println("[TL] data + timestamp: " + Arrays.toString(buf.array()));

        return buf.array();

    }

    static TransportSegment parseNetworkData(byte[] data) {

        ByteBuffer buf = ByteBuffer.wrap(data);
        int seq = buf.getInt();
        int ack = buf.getInt();
        byte flags = buf.get();

        byte[] actualData = new byte[buf.remaining()];

        for(int i = 0; buf.hasRemaining(); i++) {
            actualData[i] = buf.get();
        }

        return new TransportSegment(AirKont.toObjectArray(actualData),flags,  seq, ack);



    }

}
