package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import Messages.PutChunkMessage;
import Messages.StoredMessage;
import Shared.Peer;
import Utils.Hash;

public class PutChunkWorker implements Runnable {
    PutChunkMessage msg;
    Peer peer;
    public PutChunkWorker(PutChunkMessage msg) {
        this.msg = msg;
        this.peer = Peer.getInstance();
    }

    @Override
    public void run() {
        String dir = String.format("peer%d/%s", this.peer.getPeerId(), Hash.getHexHash(this.msg.getFileId()));
        // create directory <file id> if folder doesn't exist
        Path dirPath = Paths.get(dir);

        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Create file for the chunk
        // TODO what if the chunk already exists? Does the protocol say something
        // regarding this?
        Path filePath = Paths.get(dir + String.format("//%05d",this.msg.getChunkNo()));
        try {
            Files.write(filePath, this.msg.getRawData());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Random delay
        try {
            Thread.sleep((long) (Math.random() * 400));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Create message
        try {
            StoredMessage storedMsg = new StoredMessage(msg.getVersion(), this.peer.getPeerId(), msg.getFileId(), msg.getChunkNo());
            DatagramPacket dp = storedMsg.getDatagramPacket();
            dp.setSocketAddress(Peer.getInstance().getAddrMC().getInetSocketAddress());
            Peer.getInstance().getMcSocket().send(dp);
            System.out.println("[Sent message] " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}