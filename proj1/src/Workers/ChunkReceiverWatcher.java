package Workers;

import java.util.concurrent.ConcurrentHashMap;

import Shared.Peer;
import Utils.ConsoleColours;
import Utils.FileInfo;
import Utils.PrintMessage;
import Messages.GetChunkMessage;

/**
 * Runnable responsable to track if the expected chunks of a file to be restored have already arrived
 */
public class ChunkReceiverWatcher implements Runnable {
    // reference to the peer
    private Peer peer;
    // the file information
    FileInfo finfo;
    // reference to captured chunks through MDR channel
    ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> receivedChunkInfo;

    /**
     * 
     * @param info
     */
    public ChunkReceiverWatcher(FileInfo info) {
        this.peer = Peer.getInstance();
        this.receivedChunkInfo = this.peer.getReceivedChunkInfo();
        this.finfo = info;
    }

    @Override
    public void run() {
        int numAttempts = 0;
        boolean hasAllChunks = false;
        while (numAttempts < 5 && !hasAllChunks) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            numAttempts++;
            ConcurrentHashMap<Integer, Long> chunks = this.receivedChunkInfo.get(this.finfo.getFileIdHex());
            if(chunks == null) continue;
            
            hasAllChunks = true; // assume we have everything
            for (int i = 0; i < this.finfo.getChunks().size(); i++) {
                if (!chunks.containsKey(i)) {
                    hasAllChunks = false;
                    System.out.println("Chunk " + i + "not received yet");
                    // send message getchunk
                    GetChunkMessage msg = new GetChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(),
                            this.finfo.getFileId(), i);
                    Thread t = new Thread(new RestoreWorker(msg, i));
                    t.start();
                }
            }
        }

        if(hasAllChunks)
            PrintMessage.p("CHUNK RESTORE", String.format("Got all chunks for file %s", this.finfo.getFilename()) , ConsoleColours.GREEN_BOLD_BRIGHT, ConsoleColours.GREEN);
        else
            PrintMessage.p("CHUNK RESTORE", String.format("Failed to get all chunks for file %s. ABORT", this.finfo.getFilename()) , ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
    }
}