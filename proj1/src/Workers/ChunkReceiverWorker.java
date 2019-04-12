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

public class ChunkReceiverWorker implements Runnable {
    ChunkMessage msg;
    Peer peer;

    public ChunkReceiverWorker(ChunkMessage msg) {
        this.msg = msg;
        this.peer = Peer.getInstance();
    }

    @Override
    /**
     * send the requested chunk over the MDR
     */
    public void run() {
        
        PrintMessage.p("CHUNK", "received the following: " + msg.getFileIdHexStr() + msg.getChunkNo(), ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);

        // "Store file"
    }
}