package tp1;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
	public static void main(String[] args) {
		int port = 0;
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			System.out.println("Usage java server <port>");
		}
		// Create socket
		try {	
			InetAddress localhost = InetAddress.getLocalHost();
			DatagramSocket socket = new DatagramSocket(port, localhost);
			// Receveive requests
			byte[] buffer = new byte[30]; int length = 30;
			DatagramPacket receivedPacket = new DatagramPacket(buffer, length);
			System.out.println(receivedPacket.getData());
		} catch (Exception e) {
			//TODO: handle exception
		}
	}
}
