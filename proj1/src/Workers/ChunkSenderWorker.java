package Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

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
        PrintMessage.p("CHUNK", "File to send found: " + Files.exists(file2Send), ConsoleColours.GREEN_BOLD_BRIGHT,
                ConsoleColours.GREEN);

        // Random delay
        try {
            Long sleep = (long) (Math.random() * 400);
            System.out.println(sleep);
            Thread.sleep( sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // if this chunk has been received on the MDR, we can abort sending this
        // message.

        ConcurrentHashMap<Integer, Long> cont = this.peer.getReceivedChunkInfo().get(msg.getFileIdHexStr());
        if(cont !=null){
            Long instant = cont.get(msg.getChunkNo());
            if (instant != null) {
                // we got it
                PrintMessage.p("HALTING CHUNK",
                        "message already found sent by another peer " + (System.currentTimeMillis() - instant) + "ms ago.", ConsoleColours.RED_BOLD_BRIGHT,
                        ConsoleColours.RED);
            }
            return;
        }

        // Create message
        try {
            ChunkMessage cMsg = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), msg.getFileId(),
                    msg.getChunkNo(), Files.readAllBytes(file2Send));
            DatagramPacket dp = cMsg.getDatagramPacket();
            dp.setSocketAddress(Peer.getInstance().getAddrMDR().getInetSocketAddress());
            Peer.getInstance().getMdrSocket().send(dp);
            PrintMessage.p("Sent", cMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}