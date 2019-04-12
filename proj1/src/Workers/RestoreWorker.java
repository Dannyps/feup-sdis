package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import Messages.GetChunkMessage;
import Messages.PutChunkMessage;
import Shared.Peer;
import Utils.Chunk;
import Utils.ConsoleColours;
import Utils.FileInfo;
import Utils.PrintMessage;

public class RestoreWorker implements Callable<Integer> {
    private String protocolVersion;
    private Integer serverId;
    private Chunk chunk;
    private Peer peer;
    private String filename;
    private int chunkno;
    private GetChunkMessage msg;

    /**
     * @param msg       the pre-built message to send
     * @param chunkNo   the number of the chunk to recover
     * @param chunkList the ArrayList of chunks where to deposit the received chunk.
     */
    public RestoreWorker(GetChunkMessage msg, int chunkNo, ArrayList<Chunk> chunkList) {
        this.peer = Peer.getInstance(); // get a reference to the singleton peer
        this.protocolVersion = this.peer.getProtocolVersion();
        this.serverId = this.peer.getPeerId();
        this.msg = msg;
    }

    @Override
    public Integer call() throws Exception {
        // Get the raw message
        byte[] rawMsg = msg.getMessage();
        // create the datagram
        DatagramPacket dp = new DatagramPacket(rawMsg, rawMsg.length, this.peer.getAddrMC().getInetSocketAddress());
        //FileInfo fi = this.peer.getMyBackedUpFiles().get(filename);
        // send the PUTCHUNK message
        try {
            this.peer.getMcSocket().send(dp);
            System.out.println(ConsoleColours.CYAN + String.format("Sent getChunk (fileId,chunk) (%s, %d).",
                    this.chunk.getFileIdHexStr(), this.chunk.getChunkNo()) + ConsoleColours.RESET);
            PrintMessage.p("Sent", msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}