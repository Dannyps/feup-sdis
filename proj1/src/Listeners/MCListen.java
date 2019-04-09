package Listeners;

import java.net.MulticastSocket;
import Messages.Message;
import Messages.MessageType;
import Shared.Peer;

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
        if(msg.getMessageType() == MessageType.STORED) {
            Peer.getInstance().chunkStored(msg.getFileIdHexStr(), msg.getChunkNo(), msg.getSenderId());
        }
    }
}