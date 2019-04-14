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
    byte[] fileId;
    // the replication degree (specified when the backup was made)
    int rdegree = 0;
    Integer numChunks;
    ConcurrentHashMap<Integer, ChunkInfo> chunks;

    /**
     * 
     * @param filename  The original file name in the file system
     * @param fileIdHex The hexadecimal file hash string
     * @param chunkNo   The amount of chunks needed for this file
     */
    public FileInfo(String filename, byte[] fileId, int rdegree, int numChunks) {
        this.filename = filename;
        this.fileId = fileId;
        this.rdegree = rdegree;
        this.numChunks = numChunks;
        this.chunks = new ConcurrentHashMap<Integer, ChunkInfo>();
    }

    /**
     * Updates the list of peers who own a given chunk
     * 
     * @param chunkNo
     * @param ownerPeer
     */
    public void addChunkOwner(Integer chunkNo, Integer ownerPeer) {
        ChunkInfo c;
        if (this.chunks.containsKey(chunkNo)) {
            c = this.chunks.get(chunkNo);
        } else {
            c = new ChunkInfo(this.rdegree);
            this.chunks.put(chunkNo, c);
        }

        c.addOwnerPeer(ownerPeer);
    }

    public void removeChunkOwner(Integer chunkNo, Integer ownerPeer) {
        if (this.chunks.containsKey(chunkNo))
            this.chunks.get(chunkNo).removeOwnerPeer(ownerPeer);
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
        return fileId;
    }

    public String getFileIdHex() {
        return Hash.getHexHash(this.fileId);
    }

    /**
     * 
     * @return The number of chunks that compose this file
     */
    public Integer getNumberChunks() {
        return this.numChunks;
    }

    /**
     * 
     * @param chunkNo
     * @return The {@link Utils.ChunkInfo} object for the request chunk identifier,
     *         of null if such reference doesn't exist
     */
    public ChunkInfo getChunk(Integer chunkNo) {
        return this.chunks.get(chunkNo);
    }

    public ConcurrentHashMap<Integer, ChunkInfo> getChunks() {
        return this.chunks;
    }
}