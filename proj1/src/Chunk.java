import java.io.Serializable;

class Chunk implements Serializable {
    private String fileID;
    private Integer chunkID;
    private Integer replicationDegree;
    private byte[] data;
    
    Chunk (String fileID, Integer chunkID, Integer replicationDegree, byte[] data) {
        this.fileID = fileID;
        this.chunkID = chunkID;
        this.replicationDegree = replicationDegree;
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("Chunk <%d>, FileID: <%s>, ReplicationDegree: <%d>, Size: <%d>", this.chunkID, this.fileID, this.replicationDegree, this.data.length);
    }
}