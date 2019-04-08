package Messages;

public class StoredMessage extends Message {

    public StoredMessage(String version, Integer senderId, byte[] fileId, Integer chunkNo) {
        super(MessageType.PUTCHUNK, version, senderId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }
}