package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

import Messages.Message;
import Utils.Constants;

/**
 * MDBListen
 */
public class MDBListen implements Runnable {

    MulticastSocket socket;

    public MDBListen(MulticastSocket s) {
        socket = s;
    }

    @Override
    public void run() {
        // Listen for packets on MDR
        while (true) {
            byte[] b = new byte[Constants.UDP_MAX_SIZE];
            DatagramPacket dp = new DatagramPacket(b, Constants.UDP_MAX_SIZE);
            try {
                socket.receive(dp);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // resize the byte array to fit the exact datagram data length
            b = Arrays.copyOf(dp.getData(), dp.getLength());
            // parse the message
            Message msg = null;
            try {
                msg = Message.parseMessage(b);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.err.println("[Received message] " + msg);
        }
    }


}