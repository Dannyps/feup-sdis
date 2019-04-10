package Messages;

/**
 * Represents a CHUNK message
 */
public class ChunkMessage extends Message {
    /**
     * 
     * @param version The protocol version
     * @param senderId The sender identifier
     * @param fileId The file hash in raw bytes
     * @param chunkNo The chunk number to be requested
     * @param data The chunk raw data
     */
    public ChunkMessage(String version, Integer senderId, byte[] fileId, Integer chunkNo, byte[] data) {
        super(MessageType.GETCHUNK, version, senderId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.data = data;
    }
}