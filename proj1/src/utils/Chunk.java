package utils;

import java.io.Serializable;

public class Chunk implements Serializable {
    private byte[] fileID;
    private Integer chunkNo;
    private Integer replicationDegree;
    private byte[] data;
    
    public Chunk (byte[] fileID, Integer chunkNo, Integer replicationDegree, byte[] data) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.data = data;
    }

    @Override
    public String toString() {
        String data = Hash.getHexHash(this.data);
        return String.format("Chunk\n\tFileID: %s\n\tChunk: %d\n\tReplicationDegree: %d\n\tSize: %d\n\tData: %s...", 
            Hash.getHexHash(this.fileID), 
            this.chunkNo, 
            this.replicationDegree, 
            this.data.length,
            data.substring(0, Math.min(15, data.length())));
    }

	/**
	 * @return the fileID
	 */
	public byte[] getFileID() {
		return fileID;
	}

	/**
	 * @return the chunkID
	 */
	public Integer getChunkNo() {
		return chunkNo;
	}

	/**
	 * @return the replicationDegree
	 */
	public Integer getReplicationDegree() {
		return replicationDegree;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}
}