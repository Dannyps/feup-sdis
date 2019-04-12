package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import Messages.ChunkMessage;
import Messages.GetChunkMessage;
import Messages.PutChunkMessage;
import Messages.StoredMessage;
import Shared.Peer;
import Utils.ConsoleColours;
import Utils.PrintMessage;
import Utils.ServiceFileSystem;

public class ChunkSenderWorker implements Runnable {
    GetChunkMessage msg;
    Peer peer;

    public ChunkSenderWorker(GetChunkMessage msg) {
        this.msg = msg;
        this.peer = Peer.getInstance();
    }

    @Override
    /**
     * send the requested chunk over the MDR
     */
    public void run() {
        
        Path file2Send = Paths.get(ServiceFileSystem.getBackupChunkPath(msg.getFileIdHexStr(), msg.getChunkNo()));
        PrintMessage.p("CHUNK", "File to send found: " + Files.exists(file2Send), ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);

        // Random delay
        try {
            Thread.sleep((long) (Math.random() * 400));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create message
        try {
            ChunkMessage cMsg = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), msg.getFileId(), msg.getChunkNo(), Files.readAllBytes(file2Send));
            DatagramPacket dp = cMsg.getDatagramPacket();
            dp.setSocketAddress(Peer.getInstance().getAddrMDR().getInetSocketAddress());
            Peer.getInstance().getMdrSocket().send(dp);
            PrintMessage.p("Sent", cMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}