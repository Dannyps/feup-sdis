package tp1;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Server {
	public static void main(String[] args) {
		int srvc_port;
		String mcast_addr;
		int mcast_port;

		try {
			/* parse arguments */
			srvc_port = Integer.parseInt(args[0]);
			mcast_addr = args[1];
			mcast_port = Integer.parseInt(args[2]);
		} catch (NumberFormatException ex) {
			System.out.println("Usage java server <srvc_port> <mcast_addr> <mcast_port>");
			return;
		}

		try {
		/* open service */
		InetAddress group = InetAddress.getByName(mcast_addr);
		MulticastSocket mcast_socket = new MulticastSocket(mcast_port);
		String service_msg = new String(InetAddress.getLocalHost().toString() + ":" + srvc_port);
			/* broadcast the address and port where the service is provided */
			DatagramPacket service_packet = new DatagramPacket(service_msg.getBytes(), service_msg.length(), group , mcast_port);
			mcast_socket.send(service_packet);	
		} catch (Exception e) {
			//TODO: handle exception
		}
	}
}
