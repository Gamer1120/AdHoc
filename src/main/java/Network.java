import java.net.DatagramPacket;
import java.net.InetAddress;

public interface Network {

    public void send(InetAddress dest, byte[] data);

    public void processPacket(DatagramPacket packet);


}
