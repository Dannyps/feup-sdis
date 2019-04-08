package Listeners;

import java.net.MulticastSocket;
import Messages.Message;
import Messages.PutChunkMessage;
import Workers.PutChunkWorker;

/**
 * MDBListen
 */
public class MDBListen extends ChannelListener{
    
    /**
     * 
     * @param s Multicast channel for backup messages
     */
    public MDBListen(MulticastSocket s) {
        super(s);
    }

    @Override
    protected void newMessageHandler(Message msg) {
        // ignore self messages
        if(msg.getSenderId() != this.serverId) {
            System.err.println("[Received message] " + msg);
            PutChunkWorker w = new PutChunkWorker((PutChunkMessage) msg);
            executor.submit(w);
        }
    }
}