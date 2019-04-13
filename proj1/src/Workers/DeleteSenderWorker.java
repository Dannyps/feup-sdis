package Workers;

import java.io.IOException;
import java.net.DatagramPacket;

import Messages.DeleteMessage;
import Shared.Peer;
import Utils.FileInfo;
import Utils.PrintMessage;

public class DeleteSenderWorker implements Runnable {
    // Information regarding the file to be deleted
    private FileInfo finfo;
    // Reference to the singleton Peer
    private Peer peer;

    /**
     * Creates the worker responsible to request all peers to delete a file
     * 
     * @param finfo Information regarding the file to be deleted
     */
    public DeleteSenderWorker(FileInfo finfo) {
        this.finfo = finfo;
        this.peer = Peer.getInstance();
    }

    @Override
    public void run() {

        try {
            // create message
            DeleteMessage msg = new DeleteMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(),
                    this.finfo.getFileId());
            // send message through MC channel
            DatagramPacket dp = msg.getDatagramPacket();
            dp.setSocketAddress(this.peer.getAddrMC().getInetSocketAddress());
            this.peer.getMcSocket().send(dp);
            PrintMessage.p("Sent", msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}