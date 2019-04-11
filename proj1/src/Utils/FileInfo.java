package Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used by each peer to keepo track of own backed up files (in other
 * peers).
 */
public class FileInfo implements Serializable {
    // UID for serialization
    private static final long serialVersionUID = -4040935054301086263L;
    // The original filename from whom this chunk belongs to
    String filename;
    // the file identifier hash as hexadecimal string
    String fileIdHex;
    // the replication degree (specified when the backup was made)
    int rdegree = 0;
    // information about this file's chunks
    ConcurrentHashMap<Integer, Integer> chunkBDs;

    /**
     * 
     * @param filename  The original file name in the file system
     * @param fileIdHex The hexadecimal file hash string
     * @param chunkNo   The amount of chunks needed for this file
     */
    public FileInfo(String filename, String fileIdHex, int rdegree) {
        this.filename = filename;
        this.fileIdHex = fileIdHex;
        this.rdegree = rdegree;
        this.chunkBDs = new ConcurrentHashMap<Integer, Integer>();
    }

    public void setBD(Integer chunkNo, Integer bd) {
        this.chunkBDs.put(chunkNo, bd);
    }

    /**
     * @return the chunks
     */
    public ConcurrentHashMap<Integer, Integer> getChunks() {
        return this.chunkBDs;
    }
    /**
     * @return the desired replication degree
     */
    public int getRdegree() {
        return rdegree;
    }
}