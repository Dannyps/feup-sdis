package Listeners;

import java.net.MulticastSocket;

import Messages.GetChunkMessage;
import Messages.Message;
import Messages.MessageType;
import Utils.PrintMessage;
import Workers.ChunkSenderWorker;
import Workers.DeleteWorker;

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
        if (msg.getMessageType() == MessageType.STORED && msg.getSenderId() != this.serverId) {
            if (this.peer.getState().isLocalFileByFileId(msg.getFileIdHexStr())) {
                // if the STORED message is a response to a local file backup, update the list
                // of owner peers of the said file chunk
                this.peer.getState().addLocalFileChunkOwner(msg.getFileIdHexStr(), msg.getChunkNo(), msg.getSenderId());
            } else {
                // some peer stored some chunk, and I must keep track of it
                this.peer.getState().addChunkBackupOwner(msg.getFileIdHexStr(), msg.getChunkNo(), msg.getSenderId());
            }
        } else if (msg.getMessageType() == MessageType.GETCHUNK && msg.getSenderId() != this.serverId) {
            // launch thread to reply requested chunk
            ChunkSenderWorker w = new ChunkSenderWorker((GetChunkMessage) msg);
            executor.submit(w);
        } else if (msg.getMessageType() == MessageType.DELETE && msg.getSenderId() != this.serverId) {
            // launch thread to process the message and delete chunks of the said file
            executor.submit(new DeleteWorker(msg.getFileId()));
        }
    }
}