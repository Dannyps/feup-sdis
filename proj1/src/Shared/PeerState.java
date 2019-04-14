package Shared;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import Utils.Chunk;
import Utils.ChunkInfo;
import Utils.FileInfo;
import Utils.RegularFile;

public class PeerState {
    private long storageCapacity = (long) 64E6;
    private long storageUsed = 0;

    /**
     * Tracks local backed up files, i.e, the files that this peer has requested to
     * other peers to back up. It maps each filename, the key, to an instance
     * {@link Utils.FileInfo}, which contains the desired replication degree, the
     * actual replication degree, and more
     * 
     * @see Utils.FileInfo
     */
    private ConcurrentHashMap<String, FileInfo> localBackedUpFiles;
    /**
     * Tracks all backed up chunks. For each chunk this peer stores locally and for
     * any STORED message intercepted, a new entry is added to this hash map. The
     * key is the file identifier, in hexadecimal format. The second hash map, maps
     * the chunk number to a {@link ChunkInfo}
     * 
     * @see Utils.FileInfo
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, ChunkInfo>> storedChunks;

    public PeerState() {
        this.localBackedUpFiles = new ConcurrentHashMap<>();
        this.storedChunks = new ConcurrentHashMap<>();
    }

    /**
     * Initializes a new entry for a new local file backup
     * 
     * @param file
     * @param replicationDegree
     */
    public void addLocalFileBackup(RegularFile file, int replicationDegree) {
        FileInfo info;

        try {
            info = new FileInfo(file.getPathName(), file.getFileID(), replicationDegree, file.getChunks().size());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (this.localBackedUpFiles.containsKey(file.getPathName())) {
            System.out.println("File already backed up!");
            return;
        }
        this.localBackedUpFiles.put(file.getPathName(), info);
    }

    /**
     * Upon a STORED message in response to a PUTCHUNK message, it updates the list
     * of owners of the chunk
     * 
     * @param fileId
     * @param chunkNo
     * @param peerId
     */
    public void addLocalFileBackupChunkOwner(String fileId, Integer chunkNo, Integer ownerPeer) {
        FileInfo finfo = this.localBackedUpFiles.get(fileId);
        if (finfo != null) {
            finfo.addChunkOwner(chunkNo, ownerPeer);
        }
    }

    /**
     * Removes a peer from the list of peers who own a local backed up file chunk.
     * Useful when peers reclaim space and delete chunks (REMOVED message)
     * 
     * @param fileId
     * @param chunkNo
     * @param ownerPeer
     */
    public void removeLocalFileBackupChunkOwner(String fileId, Integer chunkNo, Integer ownerPeer) {
        FileInfo finfo = this.localBackedUpFiles.get(fileId);
        if (finfo != null) {
            finfo.removeChunkOwner(chunkNo, ownerPeer);
        }
    }

    /**
     * Registers the ocurrence of a new chunk backup request (doesn't necessarially
     * mean any peer has stored it yet)
     * 
     * @param fileId
     * @param chunkNo
     * @param replicationDegree
     */
    public void addStoreChunk(String fileId, Integer chunkNo, Integer replicationDegree) {
        ConcurrentHashMap<Integer, ChunkInfo> chunks = this.storedChunks.get(fileId);
        if (chunks == null) {
            chunks = new ConcurrentHashMap<Integer, ChunkInfo>();
            this.storedChunks.put(fileId, chunks);
        }

        ChunkInfo cinfo = chunks.get(chunkNo);
        if (cinfo == null) {
            cinfo = new ChunkInfo(replicationDegree);
            chunks.put(chunkNo, cinfo);
        }
    }

    /**
     * Sets a stored chunk reference as locally stored
     * 
     * @param fileId
     * @param chunkNo
     */
    public void setStoredChunkLocal(String fileId, Integer chunkNo) {
        ConcurrentHashMap<Integer, ChunkInfo> chunks = this.storedChunks.get(fileId);
        if (chunks != null) {
            chunks.get(chunkNo).setLocalStored();
        }
    }

    /**
     * Register that another peer has stored a file chunk
     * 
     * @param fileId
     * @param chunkNo
     * @param peerId
     */
    public void addStoredChunkOwner(String fileId, Integer chunkNo, Integer peerId) {
        ConcurrentHashMap<Integer, ChunkInfo> chunks = this.storedChunks.get(fileId);
        if (chunks != null) {
            chunks.get(chunkNo).addOwnerPeer(peerId);
        }
    }

    /**
     * Check if there's any reference for a stored chunk with pair (fileId, chunkNo)
     * 
     * @param fileId
     * @param chunkNo
     * @return
     */
    public boolean chunkStoreEntryExists(String fileId, Integer chunkNo) {
        if (this.storedChunks.containsKey(fileId)) {
            return this.storedChunks.get(fileId).containsKey(chunkNo);
        }

        return false;
    }

    /**
     * 
     * @param fileId
     * @param chunkNo
     * @return
     */
    public boolean isChunkStoredLocally(String fileId, Integer chunkNo) {
        if (this.storedChunks.containsKey(fileId)) {
            if (this.storedChunks.get(fileId).containsKey(chunkNo)) {
                return this.storedChunks.get(fileId).get(chunkNo).isStoredLocally();
            }
        }

        return false;
    }
}