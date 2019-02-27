package tp1;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Client {
    public static void main(String[] args) {
        // java Client <host_name> <port_number> <oper> <opnd>
        try {
            String hostname = args[0];
            int port = Integer.parseInt(args[1]);
            String operation = args[2];

            // Create socket
            InetAddress hostnameAddress = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket(port, hostnameAddress) ;
            // Receveive requests
            String message = new String("This is a dummy test");
            DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, hostnameAddress, port);
    
        } catch (Exception e) {
            //TODO: handle exception
        }
    }
}
