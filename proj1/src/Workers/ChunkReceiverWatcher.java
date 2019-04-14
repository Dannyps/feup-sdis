package Workers;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import Shared.Peer;
import Utils.ConsoleColours;
import Utils.FileInfo;
import Utils.PrintMessage;
import Utils.RegularFile;
import Utils.ServiceFileSystem;
import Messages.GetChunkMessage;

/**
 * Runnable responsable to track if the expected chunks of a file to be restored
 * have already arrived
 */
public class ChunkReceiverWatcher implements Runnable {
    /** reference to the singleton peer */
    private Peer peer;
    /** the file information */
    FileInfo finfo;
    /** reference to captured chunks through MDR channel */
    ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> receivedChunkInfo;

    /**
     * Creates a new watcher of the chunks being received upon a local file restore
     * request
     * 
     * @param info Information of the local file to be restored
     */
    public ChunkReceiverWatcher(FileInfo info) {
        this.peer = Peer.getInstance();
        this.receivedChunkInfo = this.peer.getReceivedChunkInfo();
        this.finfo = info;
    }

    @Override
    public void run() {
        // number of attempts so far
        int numAttempts = 0;
        // flag telling if all expected chunks have arrived
        boolean hasAllChunks = false;

        while (numAttempts < 5 && !hasAllChunks) {
            // some delay to chunks arrive
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // update number of attempts
            numAttempts++;

            // get all chunks received for the file being restored
            ConcurrentHashMap<Integer, Long> chunks = this.receivedChunkInfo.get(this.finfo.getFileIdHex());
            if (chunks == null)
                continue;

            // assume we have everything
            hasAllChunks = true;

            // loop through all expected chunks for the file
            for (int i = 0; i < this.finfo.getNumberChunks(); i++) {
                // if the chunk with id 'i' was not received yet, then request it again
                if (!chunks.containsKey(i)) {
                    // update flag
                    hasAllChunks = false;
                    // report that some chunk was not received
                    PrintMessage.p("CHUNK RESTORE",
                            String.format("Expected chunk not received yet: (%s,%d)", this.finfo.getFileIdHex(), i),
                            ConsoleColours.YELLOW_BOLD, ConsoleColours.YELLOW);
                    // send message getchunk
                    GetChunkMessage msg = new GetChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(),
                            this.finfo.getFileId(), i);
                    // launch thread that will simply send the message
                    Thread t = new Thread(new RestoreWorker(msg, i));
                    t.start();
                }
            }
        }

        // check if the loop reached the end because of all chunks received of timeout
        if (hasAllChunks) {
            // informat that all chunks were received!!
            PrintMessage.p("CHUNK RESTORE", String.format("Received all %d chunks for file %s",
                    this.finfo.getNumberChunks(), this.finfo.getFilename()), ConsoleColours.GREEN_BOLD_BRIGHT,
                    ConsoleColours.GREEN);
            // merge all chunks and write them to disk
            writeFileToDisk();
        } else
            PrintMessage.p("CHUNK RESTORE",
                    String.format("Failed to get all chunks for file %s. ABORT!", this.finfo.getFilename()),
                    ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
    }

    private void writeFileToDisk() {
        String n = ServiceFileSystem.getRestoredFilePath(this.finfo.getFilename());
        Path filePath = Paths.get(n);
        if (!Files.exists(filePath.getParent())) {
            try {
                Files.createDirectories(filePath.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.write(filePath, getAllChunksData());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Writing " + filePath);
    }

    /**
     * should only be called after hasAllChunks == true and is final
     * 
     * @return
     */
    private byte[] getAllChunksData() {
        // todo remove some of the chunks from memory upon restoring it
        System.out.println("number of chunks: " + this.peer.getReceivedChunkData().size());
        ConcurrentHashMap<Integer, byte[]> thisFilesHashMap = this.peer.getReceivedChunkData()
                .get(finfo.getFileIdHex());
        byte[] ret = new byte[thisFilesHashMap.size() * (RegularFile.getCHUNK_MAX_SIZE() + 100)];
        int finalSize = 0;
        for (Integer i = 0; i < thisFilesHashMap.size(); i++) {
            System.arraycopy(thisFilesHashMap.get(i), 0, ret, i * (RegularFile.getCHUNK_MAX_SIZE()),
                    thisFilesHashMap.get(i).length);
            finalSize += thisFilesHashMap.get(i).length;
            System.out.println(finalSize);
        }
        byte[] ret2 = new byte[finalSize];
        System.arraycopy(ret, 0, ret2, 0, finalSize);
        return ret2;
    }
}