package Workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import Shared.Peer;
import Utils.ChunkInfo;
import Utils.ConsoleColours;
import Utils.FileInfo;
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

    public static ConcurrentHashMap<String, FileInfo> loadMyBackedUpFiles(Integer peerId) {
        Path p = Paths.get(ServiceFileSystem.getBackedUpFilesPersistentDataPath(peerId));
        if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS))
            return new ConcurrentHashMap<String, FileInfo>();

        // file exists, load it
        ConcurrentHashMap<String, FileInfo> obj;
        try {
            FileInputStream fiStream = new FileInputStream(
                    ServiceFileSystem.getBackedUpFilesPersistentDataPath(peerId));
            ObjectInputStream oiStream = new ObjectInputStream(fiStream);
            obj = (ConcurrentHashMap<String, FileInfo>) oiStream.readObject();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ConcurrentHashMap<String, FileInfo>();
    }

    @Override
    public void run() {
        ConcurrentHashMap<String, FileInfo> myBackedUpFiles = Peer.getInstance().getMyBackedUpFiles();
        ConcurrentHashMap<String, ConcurrentHashMap<Integer, ChunkInfo>> backedUpChunks = Peer.getInstance()
                .getBackedUpChunks();

        while (true) {

            try {
                File f = createIfNotExists(ServiceFileSystem.getBackedUpFilesPersistentDataPath());
                FileOutputStream foStream = new FileOutputStream(f);
                ObjectOutputStream ooStream = new ObjectOutputStream(foStream);
                ooStream.writeObject(myBackedUpFiles);
                ooStream.flush();
                ooStream.close();
                foStream.close();
                PrintMessage.p("Persistent data refresh",
                        String.format("Updated %s", ServiceFileSystem.getBackedUpFilesPersistentDataPath()),
                        ConsoleColours.GREEN_BOLD, ConsoleColours.GREEN);
            } catch (Exception e) {
                PrintMessage.p("Persistent data refresh",
                        String.format("Failed to update persistent file %s",
                                ServiceFileSystem.getBackedUpFilesPersistentDataPath()),
                        ConsoleColours.RED_BOLD, ConsoleColours.RED);
                e.printStackTrace();
            }

            try {
                File f = createIfNotExists(ServiceFileSystem.getLocalChunksPersistentDataPath());
                FileOutputStream foStream = new FileOutputStream(f);
                ObjectOutputStream ooStream = new ObjectOutputStream(foStream);
                ooStream.writeObject(backedUpChunks);
                ooStream.flush();
                ooStream.close();
                foStream.close();
                PrintMessage.p("Persistent data refresh",
                        String.format("Updated %s", ServiceFileSystem.getLocalChunksPersistentDataPath()),
                        ConsoleColours.GREEN_BOLD, ConsoleColours.GREEN);
            } catch (Exception e) {
                PrintMessage.p("Persistent data refresh",
                        String.format("Failed to update persistent file %s",
                                ServiceFileSystem.getLocalChunksPersistentDataPath()),
                        ConsoleColours.RED_BOLD, ConsoleColours.RED);
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}