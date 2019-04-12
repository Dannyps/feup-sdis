package Listeners;

import java.net.MulticastSocket;

import Messages.ChunkMessage;
import Messages.Message;
import Messages.PutChunkMessage;
import Utils.PrintMessage;
import Workers.ChunkReceiverWorker;
import Workers.PutChunkWorker;

/**
 * MDBListen
 */
public class MDRListen extends ChannelListener{
    
    /**
     * 
     * @param s Multicast channel for backup messages
     */
    public MDRListen(MulticastSocket s) {
        super(s);
    }

    @Override
    protected void newMessageHandler(Message msg) {
        // ignore self messages
        if(msg.getSenderId() != this.serverId) {
            PrintMessage.p("Received", msg);
            ChunkReceiverWorker w = new ChunkReceiverWorker((ChunkMessage) msg, System.currentTimeMillis());
            executor.submit(w);
        }
    }
}