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
import Utils.Constants;

/**
 * MDBListen
 */
public class MDBListen implements Runnable {

    MulticastSocket socket;
    ExecutorService executor;

    public MDBListen(MulticastSocket s) {
        socket = s;
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
            System.err.println("[Received message] " + msg);
            WriteChunkToDisk wc2d = new WriteChunkToDisk(msg);
            executor.submit(wc2d);

        }
    }

    class WriteChunkToDisk implements Runnable {
        PutChunkMessage m;

        WriteChunkToDisk(PutChunkMessage message) {
            m = message;
        }

        public void run() {
            try {
                System.out.println("["+Thread.currentThread().getName()+"] sleeping...");
                TimeUnit.MILLISECONDS.sleep(1500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("["+Thread.currentThread().getName()+"] here I must write the chunck to disk!");
        }
    }


    private void initWritersThreadPool() {
        executor = new ThreadPoolExecutor(5, 10, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    }


}