package com.procoder.transport;

import com.procoder.util.ArrayUtils;
import com.procoder.util.Encryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

class TransportSegment {

    public static final byte SEQ_FLAG = (byte) 0B10000000;
    public static final byte ACK_FLAG = (byte) 0B01000000;
    public static final byte DIS_FLAG = (byte) 0B00100000;
    public static final byte SYN_FLAG = (byte) 0B00010000;
    public static final byte RST_FLAG = (byte) 0B00001000;
    private static final SecretKey KEY = new SecretKeySpec(Encryption.KEY, 0, Encryption.KEY.length, "AES");
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportSegment.class);
    int seq;
    int ack;
    Byte[] data;
    // |SEQ 1bit|ACK 1bit|DISCOVERY 1bit|SYN 1bit|
    byte flags;

    public TransportSegment(Byte[] data, int seq) {
        this(data);
        setSeq(seq);
    }

    private TransportSegment(Byte[] data) {
        this.data = data;
        flags = 0;
    }

    private TransportSegment(Byte[] data, byte flags, int seq, int ack) {
        this.flags = flags;
        this.seq = seq;
        this.ack = ack;
        this.data = data;
    }

    public static TransportSegment parseNetworkData(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(Encryption.getDecrypted(data, KEY));
        byte flags = buf.get();
        int seq = buf.getInt();
        int ack = buf.getInt();
        byte[] actualData = new byte[buf.remaining()];
        for (int i = 0; buf.hasRemaining(); i++) {
            actualData[i] = buf.get();
        }
        return new TransportSegment(ArrayUtils.toObjectArray(actualData),
                flags, seq, ack);
    }

    public static TransportSegment genDiscoveryPacket() {
        TransportSegment result = new TransportSegment(new Byte[0]);
        result.setDiscover();
        return result;
    }

    public boolean isDiscover() {
        return (flags & DIS_FLAG) != 0;
    }

    public boolean isSyn() {
        return (flags & SYN_FLAG) != 0;
    }

    public boolean isRST() {
        return (flags & RST_FLAG) != 0;
    }

    public boolean validSeq() {
        return (flags & SEQ_FLAG) != 0;
    }

    public boolean validAck() {
        return (flags & ACK_FLAG) != 0;
    }

    private void setDiscover() {
        flags = (byte) (flags | DIS_FLAG);
    }

    public void setRST() {
        flags = (byte) (flags | RST_FLAG);
    }

    private void setSeq(int seq) {
        this.seq = seq;
        flags = (byte) (flags | SEQ_FLAG);
    }

    public void setAck(int ack) {
        this.ack = ack;
        flags = (byte) (flags | ACK_FLAG);
    }

    public void setSyn() {
        flags = (byte) (flags | SYN_FLAG);
    }

    public byte[] toByteArray() {
        byte[] primBytes = ArrayUtils.toPrimitiveArray(data);
        ByteBuffer buf = ByteBuffer.allocate(data.length + Long.BYTES + 1);
        buf.put(flags);
        buf.putInt(seq);
        buf.putInt(ack);
        buf.put(primBytes);
        buf.flip();

        return Encryption.getEncrypted(buf.array(), KEY);
    }
}
