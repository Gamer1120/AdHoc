import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ApplicationLayer implements Application {

	@Override
	public void send(InetAddress dest, Object input) {
		if (input instanceof String) {
			NetworkLayer.send(dest, ((String) input).getBytes());
		}
	}

	@Override
	public void processPacket(DatagramPacket packet) {
		byte[] bytestream = packet.getData();
		GUI.sendString(Arrays.toString(bytestream));
	}
}
