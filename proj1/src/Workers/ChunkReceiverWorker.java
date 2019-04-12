package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import Messages.ChunkMessage;
import Messages.GetChunkMessage;
import Messages.PutChunkMessage;
import Messages.StoredMessage;
import Shared.Peer;
import Utils.ConsoleColours;
import Utils.PrintMessage;
import Utils.ServiceFileSystem;

public class ChunkReceiverWorker implements Runnable {
    ChunkMessage msg;
    Peer peer;
    Long messageArrivedAt;
    ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> receivedChunkInfo;

    public ChunkReceiverWorker(ChunkMessage msg, Long messageArrivedAt) {
        this.msg = msg;
        this.peer = Peer.getInstance();
        this.receivedChunkInfo = this.peer.getReceivedChunkInfo();
        this.messageArrivedAt = messageArrivedAt;
    }

    @Override
    /**
     * send the requested chunk over the MDR
     */
    public void run() {

        PrintMessage.p("CHUNK", "received the following: " + msg.getFileIdHexStr() + msg.getChunkNo(),
                ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
        ConcurrentHashMap<Integer, Long> thisFilesChunks = this.receivedChunkInfo.get(msg.getFileIdHexStr());
        
        if (thisFilesChunks == null) {
            // file not yet in the hashmap. Add it.
            addFileAndChunk2HashMap();

        }else{
            addChunk2HashMap(thisFilesChunks);
        }
        // "Store file"
    }

    private void addChunk2HashMap(ConcurrentHashMap<Integer, Long> thisFilesChunks) {
        thisFilesChunks.put(msg.getChunkNo(), this.messageArrivedAt);
    }

    private void addFileAndChunk2HashMap() {
        // iner hashmap
        ConcurrentHashMap<Integer, Long> inner = new ConcurrentHashMap<>();
        inner.put(msg.getChunkNo(), this.messageArrivedAt);
        // outer hashmap
        this.receivedChunkInfo.put(msg.getFileIdHexStr(), inner);
    }
}