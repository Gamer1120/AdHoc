/**
 * Application Layer for the Ad hoc multi-client chat application.
 * 
 * @author Michael Koopman s1401335, Sven Konings s1534130, Wouter ??? s???, René Boschma s???
 */
import java.io.File;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ApplicationLayer implements Application {

	/**
	 * Sends a packet to the Network Layer.
	 */
	@Override
	public void send(InetAddress dest, Object input) {
		if (input instanceof String) {
			// TODO: Add some bytes to determine whether the message is text or
			// a file.
			NetworkLayer.send(dest, ((String) input).getBytes());
		} else if (input instanceof File) {
			Path path = Paths.get(((File) input).getAbsolutePath());
			NetworkLayer.sendFile(dest, Files.readAllBytes(path));
		}
	}

	/**
	 * Sends a packet to the GUI.
	 */
	@Override
	public void processPacket(DatagramPacket packet) {
		byte[] bytestream = packet.getData();
		GUI.sendString(Arrays.toString(bytestream));
	}
}
