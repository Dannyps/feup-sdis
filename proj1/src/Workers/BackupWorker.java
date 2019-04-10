package Workers;

import java.io.IOException;
import java.net.DatagramPacket;

import Messages.PutChunkMessage;
import Shared.Peer;
import Utils.Chunk;

public class BackupWorker implements Runnable {
    private String protocolVersion;
    private Integer serverId;
    private Chunk chunk;
    private Peer peer;
    /**
     * 
     * @param protocolVersion
     * @param serverId
     * @param chunk
     */
    public BackupWorker(Chunk chunk) {
        this.chunk = chunk;
        this.peer = Peer.getInstance(); // get a reference to the singleton peer
        this.protocolVersion = this.peer.getProtocolVersion();
        this.serverId = this.peer.getPeerId();
    }

    @Override
    public void run() {
        // create the PUTCHUNK message
        PutChunkMessage msg = new PutChunkMessage(this.protocolVersion, this.serverId, this.chunk);
        // Get the raw message
        byte[] rawMsg = msg.getMessage();
        // create the datagram
        DatagramPacket dp = new DatagramPacket(rawMsg, rawMsg.length, this.peer.getAddrMDB().getInetSocketAddress());
        
        try {
           this.peer.getMdbSocket().send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
		System.err.println("[Sent message] " + msg);
    }
}