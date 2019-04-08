package Listeners;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

import Messages.Message;
import Utils.Constants;
/**
 * MDBListen
 */
/*
public class MDBListen implements Runnable {

    MulticastSocket s;

    @Override
    public void run() {

        loadSocket();
        // Listen for packets on MDR
        while (true) {
            byte[] b = new byte[Constants.UDP_MAX_SIZE];
            DatagramPacket dp = new DatagramPacket(b, Constants.UDP_MAX_SIZE);
            s.receive(dp);
            // resize the byte array to fit the exact datagram data length
            b = Arrays.copyOf(dp.getData(), dp.getLength());
            // parse the message
            Message msg = Message.parseMessage(b);
            System.err.println("[Received message] " + msg);
        }
    }

    public void loadSocket() {

    }

}*/