package Messages;

public class DeleteMessage extends Message {

    /**
     * 
     * @param version
     * @param senderId
     * @param fileId
     */
    public DeleteMessage(String version, Integer senderId, byte[] fileId) {
        super(MessageType.DELETE, version, senderId);
        this.fileId = fileId;
    }
}