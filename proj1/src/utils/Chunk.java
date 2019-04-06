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

    /**
     * Builds an hexadecimal string representation of the file hash
     * @return File identifier in hexadecimal format
     */
    private String getHexHash() {
        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < this.fileID.length; i++) {
            String hex = Integer.toHexString(0xff & this.fileID[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    @Override
    public String toString() {
        return String.format("FileID: <%s>, Chunk <%d>, ReplicationDegree: <%d>, Size: <%d>", this.getHexHash(), this.chunkNo, this.replicationDegree, this.data.length);
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