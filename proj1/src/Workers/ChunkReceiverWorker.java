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

/**
 * Handles the reception of a CHUNK message through the MDR channel
 */
public class ChunkReceiverWorker implements Runnable {
    ChunkMessage msg;
    Peer peer;
    Long messageArrivedAt;
    ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> receivedChunkInfo;
    ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>> receivedChunkData;

    public ChunkReceiverWorker(ChunkMessage msg, Long messageArrivedAt) {
        this.msg = msg;
        this.peer = Peer.getInstance();
        this.receivedChunkInfo = this.peer.getReceivedChunkInfo();
        this.receivedChunkData = this.peer.getReceivedChunkData();
        this.messageArrivedAt = messageArrivedAt;
    }

    @Override
    /**
     * send the requested chunk over the MDR
     */
    public void run() {

        PrintMessage.p("CHUNK", "received the following: " + msg.getFileIdHexStr() + "\tChunkNo:" + msg.getChunkNo(),
                ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);

        // store chunk body:
        ConcurrentHashMap<Integer, byte[]> fileChunkData = this.receivedChunkData.get(msg.getFileIdHexStr());

        if (fileChunkData == null) {
            // file not yet in the hashmap. Add it.
            System.out.println("registering file and chunk");
            registerFileAndChunkData();

        } else {
            System.out.println("registering chunk only");
            registerChunkData(fileChunkData);
        }
        System.out.println("Wrote data for " + msg.getChunkNo());
        // store chunk header:
        ConcurrentHashMap<Integer, Long> fileChunkHeaders = this.receivedChunkInfo.get(msg.getFileIdHexStr());

        if (fileChunkHeaders == null) {
            // file not yet in the hashmap. Add it.
            registerFileAndChunkHeader();

        } else {
            registerChunkHeader(fileChunkHeaders);
        }

    }

    private void registerChunkHeader(ConcurrentHashMap<Integer, Long> fileChunkHeaders) {
        fileChunkHeaders.put(msg.getChunkNo(), this.messageArrivedAt);
        this.scheduleRemoval(msg.getFileIdHexStr(), msg.getChunkNo());
    }

    private void registerFileAndChunkHeader() {
        // iner hashmap
        ConcurrentHashMap<Integer, Long> inner = new ConcurrentHashMap<>();
        inner.put(msg.getChunkNo(), this.messageArrivedAt);
        // outer hashmap
        this.receivedChunkInfo.put(msg.getFileIdHexStr(), inner);
        this.scheduleRemoval(msg.getFileIdHexStr(), msg.getChunkNo());
    }

    private void registerChunkData(ConcurrentHashMap<Integer, byte[]> fileChunkData) {
        fileChunkData.put(msg.getChunkNo(), msg.getRawData());
        this.scheduleRemoval(msg.getFileIdHexStr(), msg.getChunkNo());
    }

    private void registerFileAndChunkData() {
        // iner hashmap
        ConcurrentHashMap<Integer, byte[]> inner = new ConcurrentHashMap<>();
        inner.put(msg.getChunkNo(), msg.getRawData());
        // outer hashmap
        this.receivedChunkData.put(msg.getFileIdHexStr(), inner);
        this.scheduleRemoval(msg.getFileIdHexStr(), msg.getChunkNo());
    }

    private void scheduleRemoval(String fileIdHexStr, Integer chunkNo) {
        /**
         * TODO the entry added to this.receivedChunkInfo should be removed after, let's
         * say, 5 seconds, so that the file can be recovered again (otherwise, it will
         * just be found as sent by another peer, which may not even have anymore)
         */
    }
}