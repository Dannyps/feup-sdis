package Workers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import Messages.PutChunkMessage;
import Shared.Peer;
import Utils.Hash;

public class PutChunkWorker implements Runnable{
    PutChunkMessage msg;
    
    public PutChunkWorker(PutChunkMessage msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        String dir = String.format("peer%d/%s", Peer.getInstance().getPeerId(), Hash.getHexHash(this.msg.getFileId()));
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
        // TODO what if the chunk already exists? Does the protocol say something regarding this?
        Path filePath = Paths.get(dir + "//" + this.msg.getChunkNo());
        try {
            Files.write(filePath, this.msg.getRawData());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}