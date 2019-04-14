package Workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import Shared.Peer;
import Shared.PeerState;
import Utils.ConsoleColours;
import Utils.PrintMessage;
import Utils.ServiceFileSystem;

/**
 * Responsable to store information on disk periodically
 */
public class FileSystemWorker implements Runnable {

    public FileSystemWorker() {
    }

    private File createIfNotExists(String str) throws IOException {
        File f = new File(str);
        f.getParentFile().mkdirs();
        f.createNewFile();
        return f;
    }

    public static PeerState loadPeerState(Integer peerId) {
        Path p = Paths.get(ServiceFileSystem.getPeerStateDataPath(peerId));
        if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS))
            return new PeerState();

        // file exists, load it
        PeerState obj;
        try {
            FileInputStream fiStream = new FileInputStream(ServiceFileSystem.getPeerStateDataPath(peerId));
            ObjectInputStream oiStream = new ObjectInputStream(fiStream);
            obj = (PeerState) oiStream.readObject();
            oiStream.close();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new PeerState();
    }

    public void savePeerState(PeerState peerState) {
        String filePath = ServiceFileSystem.getPeerStateDataPath(Peer.getInstance().getPeerId());
        try {
            File f = createIfNotExists(filePath);
            FileOutputStream foStream = new FileOutputStream(f);
            ObjectOutputStream ooStream = new ObjectOutputStream(foStream);
            ooStream.writeObject(peerState);
            ooStream.flush();
            ooStream.close();
            foStream.close();
            PrintMessage.p("Persistent data refresh", String.format("Updated %s", filePath), ConsoleColours.GREEN_BOLD,
                    ConsoleColours.GREEN);
        } catch (Exception e) {
            PrintMessage.p("Persistent data refresh", String.format("Failed to update persistent file %s", filePath),
                    ConsoleColours.RED_BOLD, ConsoleColours.RED);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        PeerState peerState = Peer.getInstance().getState();

        while (true) {
            savePeerState(peerState);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}