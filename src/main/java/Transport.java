import java.net.DatagramPacket;
import java.net.InetAddress;

public interface Transport {

    public void send(InetAddress dest, byte[] data);

    public void processPacket(DatagramPacket packet);
}
