package Shared;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import Utils.ChunkInfo;
import Utils.FileInfo;
import Utils.RegularFile;

public class PeerState implements Serializable {
    private static final long serialVersionUID = -3867291910887949832L;
    private long storageCapacity; // bytes
    private long storageUsed; // bytes

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
        this.storageUsed = 0;
        this.storageCapacity = 64000 * 5; // bytes
    }

    // #region Methods for managing local file backups
    /**
     * Initializes a new entry for a new local file backup
     * 
     * @param file
     * @param replicationDegree
     */
    public void addLocalFileBackup(RegularFile file, int replicationDegree) {
        FileInfo info;

        try {
            info = new FileInfo(file.getPathName(), file.getFileId(), replicationDegree, file.getChunks().size());
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
     * Tells wether a file with a given id was backed up or not
     * 
     * @param fileId
     */
    public boolean isLocalFileByFileName(String filename) {
        return this.localBackedUpFiles.containsKey(filename);
    }

    /**
     * Check if there's a local file backup whose fileId matches the parameter
     * 
     * @param fileIdHex
     * @return
     */
    public boolean isLocalFileByFileId(String fileIdHex) {
        for (FileInfo fi : this.localBackedUpFiles.values()) {
            if (fi.getFileIdHex().equals(fileIdHex))
                return true;
        }
        return false;
    }

    /**
     * 
     * @param filename
     * @return The FileInfo object for the associated backed up file, or null if it
     *         doesn't exist
     */
    public FileInfo getLocalBackedUpFileInfo(String filename) {
        return this.localBackedUpFiles.get(filename);
    }

    /**
     * Removes an entry from the own backed up files. Useful when the initiator peer
     * deletes a file from the file system
     * 
     * @param fileId
     */
    public void removeLocalFileBackup(String fileId) {
        // if the key doesn't exist, nothing happens
        this.localBackedUpFiles.remove(fileId);
    }

    /**
     * When a peer responds to the PUTCHUNK request of this initiator peer with a
     * STORED message, update the list of peers who own a chunk of the local backed
     * up file
     * 
     * @param fileId
     * @param chunkNo
     * @param peerId
     */
    public void addLocalFileChunkOwner(String fileId, Integer chunkNo, Integer ownerPeer) {
        for (FileInfo fi : this.localBackedUpFiles.values()) {
            if (fi.getFileIdHex().equals(fileId)) {
                fi.addChunkOwner(chunkNo, ownerPeer);
            }
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
    @Deprecated
    public void removeLocalFileBackupChunkOwner(String fileId, Integer chunkNo, Integer ownerPeer) {
        // TODO same as above
        FileInfo finfo = this.localBackedUpFiles.get(fileId);
        if (finfo != null) {
            finfo.removeChunkOwner(chunkNo, ownerPeer);
        }
    }

    // #endregion

    // #region Methods for mananing other peers backed up files
    /**
     * Registers the ocurrence of a new chunk backup request (doesn't necessarially
     * mean any peer has stored it yet)
     * 
     * @param fileId
     * @param chunkNo
     * @param replicationDegree
     */
    public void addChunkBackup(String fileId, Integer chunkNo, Integer replicationDegree, Integer chunkSize) {
        ConcurrentHashMap<Integer, ChunkInfo> chunks = this.storedChunks.get(fileId);
        if (chunks == null) {
            chunks = new ConcurrentHashMap<Integer, ChunkInfo>();
            this.storedChunks.put(fileId, chunks);
        }

        ChunkInfo cinfo = chunks.get(chunkNo);
        if (cinfo == null) {
            cinfo = new ChunkInfo(replicationDegree, chunkSize);
            chunks.put(chunkNo, cinfo);
        }
    }

    /**
     * Tells wether there's free space for storing a chunk of the given size
     * 
     * @param chunkSize
     * @return
     */
    public boolean canStoreChunkLocally(Integer chunkSize) {
        return ((this.storageCapacity - this.storageUsed) >= chunkSize);
    }

    /**
     * Sets a stored chunk reference as locally stored
     * 
     * @param fileId
     * @param chunkNo
     */
    public void setChunkBackupAsLocal(String fileId, Integer chunkNo) {
        ConcurrentHashMap<Integer, ChunkInfo> chunks = this.storedChunks.get(fileId);
        if (chunks != null && chunks.containsKey(chunkNo)) {
            // ensure there's enough free space
            if (this.canStoreChunkLocally(chunks.get(chunkNo).getChunkSize())) {
                chunks.get(chunkNo).setLocalStored();
                this.storageUsed += chunks.get(chunkNo).getChunkSize();
            }

        }
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

    /**
     * Register that another peer has stored a file chunk
     * 
     * @param fileId
     * @param chunkNo
     * @param peerId
     */
    public void addChunkBackupOwner(String fileId, Integer chunkNo, Integer peerId) {
        ConcurrentHashMap<Integer, ChunkInfo> chunks = this.storedChunks.get(fileId);
        if (chunks != null && chunks.containsKey(chunkNo)) {
            chunks.get(chunkNo).addOwnerPeer(peerId);
        }
    }

    /**
     * 
     * @param fileId
     * @param chunkNo
     * @return number of peers who stored this chunk (including this peer if that's
     *         the case)
     */
    public Integer getChunkBackupNumOwners(String fileId, Integer chunkNo) {
        ConcurrentHashMap<Integer, ChunkInfo> chunks = this.storedChunks.get(fileId);
        if (chunks != null && chunks.containsKey(chunkNo)) {
            return chunks.get(chunkNo).getReplicationDegree();
        }

        return 0;
    }

    /**
     * Check if there's any reference for a stored chunk with pair (fileId, chunkNo)
     * 
     * @param fileId
     * @param chunkNo
     * @return
     */
    @Deprecated
    public boolean chunkStoreEntryExists(String fileId, Integer chunkNo) {
        if (this.storedChunks.containsKey(fileId)) {
            return this.storedChunks.get(fileId).containsKey(chunkNo);
        }

        return false;
    }

    // #endregion

    /**
     * @return the storageCapacity
     */
    public long getStorageCapacity() {
        return storageCapacity;
    }

    /**
     * @param storageCapacity the storageCapacity to set in bytes
     */
    public void setStorageCapacity(long storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    /**
     * @return the storageUsed
     */
    public long getStorageUsed() {
        return storageUsed;
    }

    /**
     * @return the localBackedUpFiles
     */
    public ConcurrentHashMap<String, FileInfo> getLocalBackedUpFiles() {
        return localBackedUpFiles;
    }

    /**
     * @return the storedChunks
     */
    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, ChunkInfo>> getStoredChunks() {
        return storedChunks;
    }
}