package Listeners;

import java.net.MulticastSocket;

import Messages.GetChunkMessage;
import Messages.Message;
import Messages.MessageType;
import Shared.Peer;
import Utils.PrintMessage;
import Workers.ChunkSenderWorker;

/**
 * MCListen
 */
public class MCListen extends ChannelListener {
    
    /**
     * 
     * @param s Multicast channel for control messages
     */
    public MCListen(MulticastSocket s) {
        super(s);
    }

    @Override
    protected void newMessageHandler(Message msg) {
        PrintMessage.p("Received", msg);
        if(msg.getMessageType() == MessageType.STORED) {
            Peer.getInstance().chunkStored(msg.getFileIdHexStr(), msg.getChunkNo(), msg.getSenderId());
        }else if(msg.getMessageType() == MessageType.GETCHUNK && msg.getSenderId() != this.serverId){
            // launch thread to reply requested chunk
            ChunkSenderWorker w = new ChunkSenderWorker((GetChunkMessage) msg);
            executor.submit(w);
        }
    }
}