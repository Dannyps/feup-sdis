package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import Messages.GetChunkMessage;
import Shared.Peer;
import Utils.ConsoleColours;
import Utils.PrintMessage;

// does it make sense to be callable?
public class RestoreWorker implements Runnable {
    private Peer peer;
    private int chunkno;
    private GetChunkMessage msg;

    /**
     * @param msg     the pre-built message to send
     * @param chunkNo the number of the chunk to recover
     */
    public RestoreWorker(GetChunkMessage msg, int chunkNo) {
        this.peer = Peer.getInstance(); // get a reference to the singleton peer
        this.msg = msg;
    }

    @Override
    public void run() {
        // Get the raw message
        byte[] rawMsg = msg.getMessage();
        // create the datagram
        DatagramPacket dp = new DatagramPacket(rawMsg, rawMsg.length, this.peer.getAddrMC().getInetSocketAddress());
        // FileInfo fi = this.peer.getMyBackedUpFiles().get(filename);
        // send the PUTCHUNK message
        try {
            this.peer.getMcSocket().send(dp);
            System.out.println(ConsoleColours.CYAN
                    + String.format("Sent getChunk (fileId,chunk) (%s, %d).", this.msg.getFileIdHexStr(), this.chunkno)
                    + ConsoleColours.RESET);
            PrintMessage.p("Sent", msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}