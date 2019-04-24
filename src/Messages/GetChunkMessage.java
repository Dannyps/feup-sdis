package Messages;

/**
 * Represents a GETCHUNK message
 */
public class GetChunkMessage extends Message {
    /**
     * 
     * @param version The protocol version
     * @param senderId The sender identifier
     * @param fileId The file hash in raw bytes
     * @param chunkNo The chunk number to be requested
     */
    public GetChunkMessage(String version, Integer senderId, byte[] fileId, Integer chunkNo) {
        super(MessageType.GETCHUNK, version, senderId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }
}