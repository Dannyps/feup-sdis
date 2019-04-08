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
import Shared.Peer;
import Utils.Constants;

public abstract class ChannelListener implements Runnable {

    protected MulticastSocket socket; /** channel to be listened */
    protected ExecutorService executor; /** the thread pool manager */
    protected Integer serverId; /** the peer/server id */
    
    /**
     * 
     * @param s The Multicast channel to be listened
     */
    public ChannelListener(MulticastSocket s) {
        this.socket = s;
        this.serverId = Peer.getInstance().getPeerId();
    }
    
    /**
     * Creates a thread pool and launches the core threads
     */
    protected void initWorkersThreadPool() {
        /**
         * 5 core threads, always running
         * 10 maximum threads
         * Sleeping threads are alive for 30 seconds, then they are killed
         */
        executor = new ThreadPoolExecutor(5, 10, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Handler to be called upon a new message received on the channel
     * @param msg The parsed Message
     */
    protected abstract void newMessageHandler(Message msg);

    /**
     * Launch thread
     */
    @Override
    public void run() {
        // setup thread pool
        this.initWorkersThreadPool();

        // Listen for packets on the multicast channel
        while (true) {
            byte[] b = new byte[Constants.UDP_MAX_SIZE];
            DatagramPacket dp = new DatagramPacket(b, Constants.UDP_MAX_SIZE);
            
            try {
                socket.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // resize the byte array to fit the exact datagram data length
            b = Arrays.copyOf(dp.getData(), dp.getLength());
            
            // parse the message
            Message msg = null;
            try {
                msg = Message.parseMessage(b);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // call message handler
            this.newMessageHandler(msg);
        }
    }


}