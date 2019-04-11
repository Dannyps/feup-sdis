package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.TreeSet;

import Messages.PutChunkMessage;
import Shared.Peer;
import Utils.Chunk;
import Utils.ConsoleColours;
import Utils.FileInfo;
import Utils.PrintMesssage;

public class BackupWorker implements Runnable {
    private String protocolVersion;
    private Integer serverId;
    private Chunk chunk;
    private Peer peer;
    private String filename;
    private int chunkno;

    /**
     * 
     * @param protocolVersion
     * @param serverId
     * @param chunk
     */
    public BackupWorker(String fname, Chunk chunk, int chunkno) {
        this.chunk = chunk;
        this.peer = Peer.getInstance(); // get a reference to the singleton peer
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
        FileInfo fi = this.peer.getMyBackedUpFiles().get(filename);
        while (numberTries < 5 && !isReplicationDegreeMet) {
            // send the PUTCHUNK message
            try {
                this.peer.getMdbSocket().send(dp);
                System.out.println(ConsoleColours.CYAN
                        + String.format("Sent putchunk (fileId,chunk) (%s, %d). Attempt : %d",
                                this.chunk.getFileIdHexStr(), this.chunk.getChunkNo(), numberTries + 1)
                        + ConsoleColours.RESET);
                PrintMesssage.p("Sent", msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // sleep the specified delay (remainingDelay)
            try {
                Thread.sleep(waitingDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // get list of peers which stored the chunk successfully
            TreeSet<Integer> peers = this.peer.getPeersContainChunk(this.chunk.getFileIdHexStr(),
                    this.chunk.getChunkNo());
            System.out.println(ConsoleColours.CYAN
                    + String.format("Number of peers who stored (fileId,chunk) (%s, %d): %d",
                            this.chunk.getFileIdHexStr(), this.chunk.getChunkNo(), peers.size())
                    + ConsoleColours.RESET);

            fi.setBD(this.chunkno, peers.size());

            if (peers == null || peers.size() < this.chunk.getReplicationDegree()) {
                // replication degree not met
                System.out.println(
                    ConsoleColours.YELLOW +
                    String.format("Replication degree not met yet for (fileId,chunk): (%s, %d)", 
                        this.chunk.getFileIdHexStr(),
                        this.chunk.getChunkNo()
                    ) + ConsoleColours.RESET
                );
                numberTries++;
                waitingDelay *= 2;
            } else {
                isReplicationDegreeMet = true;
            }
        }

        if(isReplicationDegreeMet) {
            System.out.println(
                ConsoleColours.GREEN + 
                String.format("Replication degree MET for (fileId,chunk): (%s, %d)", 
                    this.chunk.getFileIdHexStr(),
                    this.chunk.getChunkNo()
                ) + ConsoleColours.RESET
            );
        } else {
            System.out.println(
                ConsoleColours.RED + 
                String.format("Replication degree NOT MET for (fileId,chunk): (%s, %d)", 
                    this.chunk.getFileIdHexStr(),
                    this.chunk.getChunkNo()
                ) + ConsoleColours.RESET
            );
        }
        PrintMesssage.p("Local info", "chunk " + chunkno + " got a replication degree of " + Integer.toString(fi.getChunks().get(chunkno)) + ". Desired: "+Integer.toString(fi.getRdegree()), ConsoleColours.PURPLE_BOLD_BRIGHT, ConsoleColours.PURPLE_BRIGHT);
    }
}