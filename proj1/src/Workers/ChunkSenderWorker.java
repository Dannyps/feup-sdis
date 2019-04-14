package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import Messages.ChunkMessage;
import Messages.GetChunkMessage;
import Shared.Peer;
import Utils.ConsoleColours;
import Utils.PrintMessage;
import Utils.ServiceFileSystem;

public class ChunkSenderWorker implements Runnable {
    private GetChunkMessage msg;
    private Peer peer;

    public ChunkSenderWorker(GetChunkMessage msg) {
        this.msg = msg;
        this.peer = Peer.getInstance();
    }

    @Override
    /**
     * send the requested chunk over the MDR
     */
    public void run() {
        // check if the requested chunk was backed up by this peer
        if (!this.peer.getState().isChunkStoredLocally(this.msg.getFileIdHexStr(), this.msg.getChunkNo())) {
            PrintMessage.p("CHUNK", String.format("I don't own the requested chunk: (%s, %d)",
                    this.msg.getFileIdHexStr(), this.msg.getChunkNo()), ConsoleColours.YELLOW_BOLD_BRIGHT,
                    ConsoleColours.YELLOW);
            return;
        }

        // Random delay
        try {
            Long sleep = (long) (Math.random() * 400);
            System.out.println(sleep);
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // check if any other peer has already sent the requested chunk
        ConcurrentHashMap<Integer, Long> cont = this.peer.getReceivedChunkInfo().get(msg.getFileIdHexStr());
        if (cont != null) {
            Long instant = cont.get(msg.getChunkNo());
            if (instant != null) {
                // we got it
                PrintMessage.p(
                        "HALTING CHUNK", "message already found sent by another peer "
                                + (System.currentTimeMillis() - instant) + "ms ago.",
                        ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
            }
            return;
        }

        try {
            // create the path for the chunk stored locally
            Path file2Send = Paths.get(ServiceFileSystem.getBackupChunkPath(msg.getFileIdHexStr(), msg.getChunkNo()));
            PrintMessage.p("CHUNK", "File to send found: " + Files.exists(file2Send), ConsoleColours.GREEN_BOLD_BRIGHT,
                    ConsoleColours.GREEN);
            // create the message
            ChunkMessage cMsg = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), msg.getFileId(),
                    msg.getChunkNo(), Files.readAllBytes(file2Send));
            DatagramPacket dp = cMsg.getDatagramPacket();
            dp.setSocketAddress(Peer.getInstance().getAddrMDR().getInetSocketAddress());
            Peer.getInstance().getMdrSocket().send(dp);
            PrintMessage.p("Sent", cMsg);
        } catch (InvalidPathException e) {
            PrintMessage.p("CHUNK", "Cound't find expected chunk on the file system. Perhaps it was deleted?",
                    ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}