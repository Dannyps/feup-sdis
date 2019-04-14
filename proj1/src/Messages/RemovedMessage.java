package Messages;

public class RemovedMessage extends Message {

    public RemovedMessage(String version, Integer senderId, byte[] fileId, Integer chunkNo) {
        super(MessageType.REMOVED, version, senderId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }
}