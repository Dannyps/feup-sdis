package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Messages.Message;
import Messages.PutChunkMessage;
import Shared.Peer;
import Utils.Constants;
import Workers.PutChunkWorker;

/**
 * MDBListen
 */
public class MDBListen implements Runnable {

    private MulticastSocket socket;
    private ExecutorService executor;
    private Integer serverId;
    
    public MDBListen(MulticastSocket s, Integer serverId) {
        this.socket = s;
        this.serverId = serverId;
    }

    @Override
    public void run() {

        initWritersThreadPool();

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
            PutChunkMessage msg = null;
            try {
                msg = (PutChunkMessage) Message.parseMessage(b);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // ignore self messages
            if(msg.getSenderId() != this.serverId) {
                System.err.println("[Received message] " + msg);
                PutChunkWorker w = new PutChunkWorker(msg);
                executor.submit(w);
            }
        }
    }

    private void initWritersThreadPool() {
        executor = new ThreadPoolExecutor(5, 10, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }


}