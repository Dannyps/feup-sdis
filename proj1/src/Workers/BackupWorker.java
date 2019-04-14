package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import Messages.PutChunkMessage;
import Shared.Peer;
import Shared.PeerState;
import Utils.Chunk;
import Utils.ChunkInfo;
import Utils.ConsoleColours;
import Utils.FileInfo;
import Utils.PrintMessage;

public class BackupWorker implements Runnable {
    private String protocolVersion;
    private Integer serverId;
    private Chunk chunk;
    private Peer peer;
    private String filename;
    private int chunkno;
    private PeerState peerState;

    /**
     * 
     * @param protocolVersion
     * @param serverId
     * @param chunk
     */
    public BackupWorker(String fname, Chunk chunk, int chunkno) {
        this.chunk = chunk;
        this.peer = Peer.getInstance(); // get a reference to the singleton peer
        this.peerState = Peer.getInstance().getState();
        this.protocolVersion = this.peer.getProtocolVersion();
        this.serverId = this.peer.getPeerId();
        this.filename = fname;
        this.chunkno = chunkno;
    }

    @Override
    public void run() {
        int numberTries = 0; // the current attempt number
        int waitingDelay = 1000; // the delay for the current attempt
        boolean isReplicationDegreeMet = false; // flag to tell if the replication degree is already met or not

        // create the PUTCHUNK message
        PutChunkMessage msg = new PutChunkMessage(this.protocolVersion, this.serverId, this.chunk);
        // Get the raw message
        byte[] rawMsg = msg.getMessage();
        // create the datagram
        DatagramPacket dp = new DatagramPacket(rawMsg, rawMsg.length, this.peer.getAddrMDB().getInetSocketAddress());
        FileInfo finfo = this.peerState.getLocalBackedUpFileInfo(filename);
        while (numberTries < 5 && !isReplicationDegreeMet) {
            // send the PUTCHUNK message
            try {
                this.peer.getMdbSocket().send(dp);
                System.out.println(ConsoleColours.CYAN
                        + String.format("Sent putchunk (fileId,chunk) (%s, %d). Attempt : %d",
                                this.chunk.getFileIdHexStr(), this.chunk.getChunkNo(), numberTries + 1)
                        + ConsoleColours.RESET);
                PrintMessage.p("Sent", msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // sleep the specified delay (remainingDelay)
            try {
                Thread.sleep(waitingDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // get number of peers who acknowledged that they have stored the chunk
            Integer peersAck = finfo.getChunk(this.chunkno).getBackupDegree();
            PrintMessage.p("CHUNK ACK", String.format("Number of peers who stored (fileId,chunk) (%s, %d): %d",
                    this.chunk.getFileIdHexStr(), this.chunk.getChunkNo(), peersAck));

            // TODO
            // fi.setBD(this.chunkno, peers.size());

            if (peersAck < this.chunk.getReplicationDegree()) {
                // update number of tries and waiting delay
                numberTries++;
                waitingDelay *= 2;
                // replication degree not met
                if (numberTries != 5)
                    PrintMessage.p("CHUNK BACKUP",
                            String.format("Replication degree not met yet for (fileId,chunk): (%s, %d)",
                                    this.chunk.getFileIdHexStr(), this.chunk.getChunkNo()),
                            ConsoleColours.YELLOW_BOLD_BRIGHT, ConsoleColours.YELLOW);

            } else {
                isReplicationDegreeMet = true;
            }
        }

        // display wether the chunk was successfully replicated among the peers or not
        if (isReplicationDegreeMet) {
            PrintMessage.p(
                    "CHUNK BACKUP", String.format("Replication degree MET for (fileId,chunk): (%s, %d)",
                            this.chunk.getFileIdHexStr(), this.chunk.getChunkNo()),
                    ConsoleColours.GREEN_BOLD_BRIGHT, ConsoleColours.GREEN);
        } else {
            PrintMessage.p("CHUNK BACKUP",
                    String.format("Replication degree NOT MET for (fileId,chunk): (%s, %d)",
                            this.chunk.getFileIdHexStr(), this.chunk.getChunkNo()),
                    ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
        }

        // summary
        PrintMessage.p("Local info",
                "File: " + finfo.getFilename() + " chunk " + chunkno + " got a replication degree of "
                        + finfo.getChunk(chunkno).getBackupDegree() + ". Desired: "
                        + Integer.toString(finfo.getRdegree()),
                ConsoleColours.PURPLE_BOLD_BRIGHT, ConsoleColours.PURPLE_BRIGHT);
    }
}