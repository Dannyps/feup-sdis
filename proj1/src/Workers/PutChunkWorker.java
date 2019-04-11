package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import Messages.PutChunkMessage;
import Messages.StoredMessage;
import Shared.Peer;
import Utils.PrintMesssage;
import Utils.ServiceFileSystem;

public class PutChunkWorker implements Runnable {
    PutChunkMessage msg;
    Peer peer;
    public PutChunkWorker(PutChunkMessage msg) {
        this.msg = msg;
        this.peer = Peer.getInstance();
    }

    @Override
    public void run() {
        // create directory <file id> if folder doesn't exist
        Path dirPath = Paths.get(ServiceFileSystem.getBackupFilePath(this.msg.getFileIdHexStr()));

        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create file for the chunk
        // TODO what if the chunk already exists? Does the protocol say something
        // regarding this?
        Path filePath = Paths.get(ServiceFileSystem.getBackupChunkPath(this.msg.getFileIdHexStr(), this.msg.getChunkNo()));
        try {
            Files.write(filePath, this.msg.getRawData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Random delay
        try {
            Thread.sleep((long) (Math.random() * 400));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create message
        try {
            StoredMessage storedMsg = new StoredMessage(msg.getVersion(), this.peer.getPeerId(), msg.getFileId(), msg.getChunkNo());
            DatagramPacket dp = storedMsg.getDatagramPacket();
            dp.setSocketAddress(Peer.getInstance().getAddrMC().getInetSocketAddress());
            Peer.getInstance().getMcSocket().send(dp);
            PrintMesssage.p("Sent", msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}