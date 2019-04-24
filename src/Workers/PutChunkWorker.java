package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import Messages.PutChunkMessage;
import Messages.StoredMessage;
import Shared.Peer;
import Shared.PeerState;
import Utils.ConsoleColours;
import Utils.PrintMessage;
import Utils.ServiceFileSystem;

public class PutChunkWorker implements Runnable {
    PutChunkMessage msg;
    PeerState peerState;
    Peer peer;

    public PutChunkWorker(PutChunkMessage msg) {
        this.msg = msg;
        this.peer = Peer.getInstance();
        this.peerState = this.peer.getState();
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

        // add a new reference for tracking this chunk, if it doesn't exist already
        this.peerState.addChunkBackup(fileIdHex, chunkNo, this.msg.getReplicationDegree(),
                this.msg.getRawData().length);

        // check if the chunk is already backed up locally
        // if not, store the chunk locally
        if (!this.peerState.isChunkStoredLocally(fileIdHex, chunkNo)) {
            // check for available disk space
            if (!this.peerState.canStoreChunkLocally(this.msg.getRawData().length)) {
                // not enough space
                PrintMessage.p("REJECT CHUNK",
                        String.format("Not enough space available for storing the chunk (%s, %d)", fileIdHex, chunkNo),
                        ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
                return;
            }
            // not local and space is available, attempt to write it to the local file
            // system
            if (!storeChunk(fileIdHex, chunkNo))
                return; // failed to store chunk on disk
            // chunk is now locally stored, update ChunkInfo and set it as locally stored
            this.peerState.setChunkBackupAsLocal(fileIdHex, chunkNo);
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