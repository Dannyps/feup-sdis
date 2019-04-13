package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import Messages.PutChunkMessage;
import Messages.StoredMessage;
import Shared.Peer;
import Utils.ChunkInfo;
import Utils.PrintMessage;
import Utils.ServiceFileSystem;

public class PutChunkWorker implements Runnable {
    PutChunkMessage msg;
    Peer peer;

    public PutChunkWorker(PutChunkMessage msg) {
        this.msg = msg;
        this.peer = Peer.getInstance();
    }

    /**
     * Stores the chunk locally, respecting the specified file system organization
     * 
     * @return True if successfully stored the chunk locally
     */
    private boolean storeChunk(String fileIdHex, Integer ChunkNo) {
        // create directory <file id> if folder doesn't exist
        Path dirPath = Paths.get(ServiceFileSystem.getBackupFilePath(fileIdHex));

        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Create file for the chunk
        try {
            Path filePath = Paths.get(ServiceFileSystem.getBackupChunkPath(fileIdHex, ChunkNo));
            Files.write(filePath, this.msg.getRawData());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void run() {
        String fileIdHex = this.msg.getFileIdHexStr();
        Integer chunkNo = this.msg.getChunkNo();

        // check if the chunk is already backed up locally
        // if not, store the chunk locally
        if (!Peer.getInstance().isChunkLocal(fileIdHex, chunkNo)) {
            if (!storeChunk(fileIdHex, chunkNo))
                return; // failed to store chunk
            // registry the chunk
            Peer.getInstance().registerLocalChunk(fileIdHex, chunkNo, new ChunkInfo(this.msg.getReplicationDegree()));
        }

        // Random delay to avoid flooding Initiator peer with STORED messages
        try {
            Thread.sleep((long) (Math.random() * 400));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create message and send it
        try {
            StoredMessage storedMsg = new StoredMessage(msg.getVersion(), this.peer.getPeerId(), msg.getFileId(),
                    msg.getChunkNo());
            DatagramPacket dp = storedMsg.getDatagramPacket();
            dp.setSocketAddress(Peer.getInstance().getAddrMC().getInetSocketAddress());
            Peer.getInstance().getMcSocket().send(dp);
            PrintMessage.p("Sent", storedMsg);
        } catch (IOException e) {
            // TODO delete upon failure
            e.printStackTrace();
        }
    }
}