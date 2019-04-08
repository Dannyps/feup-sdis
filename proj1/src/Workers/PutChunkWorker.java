package Workers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import Messages.PutChunkMessage;
import Utils.Hash;

public class PutChunkWorker implements Runnable{
    PutChunkMessage msg;
    
    public PutChunkWorker(PutChunkMessage msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        // create directory <file id> if folder doesn't exist
        Path dir = Paths.get(Hash.getHexHash(this.msg.getFileId()));

        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Create file for the chunk
        // TODO what if the chunk already exists? Does the protocol say something regarding this?
        Path filePath = Paths.get(Hash.getHexHash(this.msg.getFileId()) + "//" + this.msg.getChunkNo());
        try {
            Files.write(filePath, this.msg.getRawData());
            System.out.println(String.format("Writing chunk no: %d | Size: %d", this.msg.getChunkNo(), this.msg.getRawData().length));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}