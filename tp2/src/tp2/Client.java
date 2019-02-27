package tp1;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;

public class Client {
    public static void main(String[] args) {

        String mcast_addr = "", operation = "";
        int mcast_port = 0;

        try {
            mcast_addr = args[0];
            mcast_port = Integer.parseInt(args[1]);
            operation = args[2];
            if (operation != "register" && operation != "lookup")
                throw new Exception("ai");
        } catch (Exception e) {
            // TODO: handle exception
        }

        try {

            /* listen server and wait for service address and port */
            MulticastSocket mcast_socket = new MulticastSocket(mcast_port);
            InetAddress mcast_group = InetAddress.getByName(mcast_addr);
            mcast_socket.joinGroup(mcast_group);
            byte[] buf = new byte[30];

            // Receive the information and print it.

            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
            mcast_socket.receive(msgPacket);

            String msg = new String(buf, 0, buf.length);
            System.out.println("Socket 1 received msg: " + msg);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
}
