import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class TimestampTransport implements Transport {

    /**
     *
     */

    // ------------------ Instance variables ----------------

    private Network networkLayer;

    private Queue<byte[]> unAckedData;

    private Map<InetAddress, Queue<Byte>> sendQueues;

    private boolean running;

    // ------------------- Constructors ---------------------

    public TimestampTransport(Network networkLayer) {

        this.networkLayer = null;
        running = true;

    }


    // ----------------------- Queries ----------------------


    // ----------------------- Commands ---------------------


    @Override
    public void send(InetAddress dest, byte[] data) {

        Queue<Byte> queue = sendQueues.get(dest);

        for(byte b : data) {
            queue.add(b);
        }

        processSendQueue();

    }

    @Override
    public void processPacket(DatagramPacket packet) {

        InetAddress source = packet.getAddress();
        byte[] data = packet.getData();




    }

    public void processSendQueue() {

        for(Map.Entry<InetAddress, Queue<Byte>> entry : sendQueues.entrySet()) {

            List<Byte> data = new LinkedList<>();

            while(entry.getValue().poll() != null && data.size() < 1400) {
                data.add(entry.getValue().remove());
            }

            networkLayer.send(entry.getKey(), new TransportSegment(data.toArray(new Byte[data.size()])).toByteArray());


        }

    }



    class TransportSegment {

        long timeStamp;
        Byte[] data;

        TransportSegment(Byte[] data) {

            timeStamp = System.currentTimeMillis();
            this.data = data;

        }

        byte[] toByteArray() {
            byte[] primBytes = new byte[data.length];
            int i = 0;
            for (Byte b : data) {
                primBytes[i] = b;
            }

            ByteBuffer buf = ByteBuffer.allocate(data.length + Long.BYTES);
            buf.put(primBytes);
            buf.putLong(timeStamp);

            return buf.array();

        }

    }



}
