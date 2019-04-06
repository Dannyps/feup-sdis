import java.nio.charset.StandardCharsets;

public class Message {
    /** Required fields */
    private MessageType messageType;
    private String version;
    private Integer senderId;

    /** Optional fields */
    private byte[] fileId = null;
    private Integer chunkNo = -1;
    private Integer replicationDegree = -1;

    /**
     * Converts a Java string to an array of bytes. Each byte represents a string character in 7-bit ASCII format
     * Note: Byte values are signed
     * @param str
     * @return
     */
    private byte[] stringToASCII_(String str) {
        return str.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * @return Returns the message type in ASCII bytes
     */
    private byte[] getMsgTypeASCII_() {
        return this.stringToASCII_(this.messageType.toString());
    };

    /**
     * @return Returns the version in format '<n>.<m>' in ASCII bytes
     */
    private byte[] getVersionASCII_() {
        return this.stringToASCII_(this.version);
    };

    /**
     * @return Returns the server/sender/peer identifier in ASCII bytes 
     */
    private byte[] getSenderIdASCII_() {
        return this.stringToASCII_(this.senderId.toString());
    };
    
    /**
     * @return Returns the file identifier such that each hash byte is represented by two ASCII bytes
     */
    private byte[] getFileIdASCII_() {

        byte[] b = new byte[64];
        for (int i = 0; i < this.fileId.length; i++) {
            byte[] aux = Integer.toHexString(0xff & this.fileId[i]).getBytes(StandardCharsets.US_ASCII);
            int j = i*2;
            if(aux.length == 1) {
                b[j] = '0';
                b[j+1] = aux[0];
            } else {
                b[j] = aux[0];
                b[j+1] = aux[1];
            }
        }
        //String s = new String(b, StandardCharsets.US_ASCII);
        //System.out.println(s);
        return b;
    }

    /**
     * @return Returns the chunk number as a sequence of ASCII bytes
     */
    private byte[] getChunkNoASCII_() {
        return this.stringToASCII_(this.chunkNo.toString());
    }

    /**
     * @return Returns the chunk number as a sequence of ASCII bytes
     */
    private byte[] getReplicationDegreeASCII_() {
        return this.stringToASCII_(this.replicationDegree.toString());
    }

    /**
     * 
     * @param type
     * @param version
     * @param senderId
     */
    Message(MessageType type, String version, Integer senderId) {
        this.messageType = type;
        this.version = version;
        this.senderId = senderId;
    }


}