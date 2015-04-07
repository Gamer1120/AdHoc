import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;

public class SuperTransport implements Transport {

    /**
     *
     */

    // ------------------ Instance variables ----------------

    private Network networkLayer;

    private Queue<Byte[]> unAckedData;

    private Map<InetAddress, Queue<Byte>> sendQueues;

    // ------------------- Constructors ---------------------

    public SuperTransport(Network networkLayer) {

        this.networkLayer = networkLayer;

    }


    // ----------------------- Queries ----------------------


    // ----------------------- Commands ---------------------


    @Override
    public void send(InetAddress dest, byte[] data) {


        TransportSegment newPacket = new TransportSegment(data);
        //sendQueues.get(dest).add(newPacket.toByteArray());

        networkLayer.send(dest, data);

    }

    @Override
    public void processPacket(DatagramPacket packet) {

    }

    class TransportSegment {

        long timeStamp;
        byte[] data;

        TransportSegment(byte[] data) {

            timeStamp = System.currentTimeMillis();
            this.data = data;

        }

        byte[] toByteArray() {
            ByteBuffer buf = ByteBuffer.allocate(data.length + Long.BYTES);
            buf.put(data);
            buf.putLong(timeStamp);
            return buf.array();

        }

    }



}
