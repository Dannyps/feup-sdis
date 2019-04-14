package Utils;

import java.io.Serializable;
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
    byte[] fileIdHex;
    // the replication degree (specified when the backup was made)
    int rdegree = 0;
    Integer numChunks;
    ConcurrentHashMap<Integer, ChunkInfo> chunks;
    // information about this file's chunks
    ConcurrentHashMap<Integer, Integer> chunkBDs;

    /**
     * 
     * @param filename  The original file name in the file system
     * @param fileIdHex The hexadecimal file hash string
     * @param chunkNo   The amount of chunks needed for this file
     */
    public FileInfo(String filename, byte[] fileIdHex, int rdegree, int numChunks) {
        this.filename = filename;
        this.fileIdHex = fileIdHex;
        this.rdegree = rdegree;
        this.numChunks = numChunks;
        this.chunkBDs = new ConcurrentHashMap<Integer, Integer>();
    }

    /**
     * Updates the list of peers who own a given chunk
     * 
     * @param chunkNo
     * @param ownerPeer
     */
    public void addChunkOwner(Integer chunkNo, Integer ownerPeer) {
        if (this.chunks.containsKey(chunkNo))
            this.chunks.get(chunkNo).addOwnerPeer(ownerPeer);
    }

    public void removeChunkOwner(Integer chunkNo, Integer ownerPeer) {
        if (this.chunks.containsKey(chunkNo))
            this.chunks.get(chunkNo).removeOwnerPeer(ownerPeer);
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

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    public byte[] getFileId() {
        return fileIdHex;
    }

    public String getFileIdHex() {
        return Hash.getHexHash(this.fileIdHex);
    }
}