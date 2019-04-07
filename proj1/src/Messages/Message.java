package Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class Message {
    /** Required fields */
    protected MessageType messageType;
    protected String version;
    protected Integer senderId;

    /** Optional fields */
    protected byte[] fileId = null;
    protected Integer chunkNo = null;
    protected Integer replicationDegree = null;

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

    /**
     * Converts a Java string to an array of bytes. Each byte represents a string
     * character in 7-bit ASCII format Note: Byte values are signed
     * 
     * @param str
     * @return
     */
    protected byte[] stringToASCII_(String str) {
        return str.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * @return Returns the message type in ASCII bytes
     */
    protected byte[] getMsgTypeASCII_() {
        return this.stringToASCII_(this.messageType.toString());
    };

    /**
     * @return Returns the version in format '<n>.<m>' in ASCII bytes
     */
    protected byte[] getVersionASCII_() {
        return this.stringToASCII_(this.version);
    };

    /**
     * @return Returns the server/sender/peer identifier in ASCII bytes
     */
    protected byte[] getSenderIdASCII_() {
        return this.stringToASCII_(this.senderId.toString());
    };

    /**
     * @return Returns the file identifier such that each hash byte is represented
     *         by two ASCII bytes
     */
    protected byte[] getFileIdASCII_() {

        byte[] b = new byte[64];
        for (int i = 0; i < this.fileId.length; i++) {
            byte[] aux = Integer.toHexString(0xff & this.fileId[i]).getBytes(StandardCharsets.US_ASCII);
            int j = i * 2;
            if (aux.length == 1) {
                b[j] = '0';
                b[j + 1] = aux[0];
            } else {
                b[j] = aux[0];
                b[j + 1] = aux[1];
            }
        }
        // String s = new String(b, StandardCharsets.US_ASCII);
        // System.out.println(s);
        return b;
    }

    /**
     * @return Returns the chunk number as a sequence of ASCII bytes
     */
    protected byte[] getChunkNoASCII_() {
        return this.stringToASCII_(this.chunkNo.toString());
    }

    /**
     * @return Returns the chunk number as a sequence of ASCII bytes
     */
    protected byte[] getReplicationDegreeASCII_() {
        return this.stringToASCII_(this.replicationDegree.toString());
    }

    /**
     * @return Returns the <CRLF> terminator sequence
     */
    protected byte[] getSequenceTerminator() {
        return new byte[]{'\r', '\n'};
    }

    /**
     * Creates the header following the protocol specification. In addition to the mandatory/common fields between sub-protocols, it considers aditional not-null fields. It assumes the following sequence:
     * <Message type> <Version> <SenderId> <FileId> <ChunkNo> <ReplDegree> <CRLF><CRLF>
     * 
     * @return
     */
    protected byte[] createHeader() {
        // Dynamic byte buffer
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        
        // Generic format
        // <Message type> <Version> <SenderId> <FileId> <ChunkNo> <ReplDegree> <CRLF><CRLF>
        
        try {
            // Mandatory fields
            buf.write(this.getMsgTypeASCII_());
            buf.write(' ');

            buf.write(this.getVersionASCII_());
            buf.write(' ');
            
            buf.write(this.getSenderIdASCII_());
            buf.write(' ');
            
            buf.write(this.getFileIdASCII_());
            buf.write(' ');

            // Subprotocol required fields
            if(this.chunkNo != null) {
                buf.write(this.getChunkNoASCII_());
                buf.write(' ');
            }

            if(this.replicationDegree != null) {
                buf.write(this.getReplicationDegreeASCII_());
                buf.write(' ');
            }

            // Terminator sequence
            buf.write(this.getSequenceTerminator());
            buf.write(this.getSequenceTerminator());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buf.toByteArray();
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

    /**
     * Returns the message bytes
     * Note: Only the header is returned. Sub-protocols should override this method to include the body data
     * @return
     */
    public byte[] getMessage() {
        return this.createHeader();
    }


    /**
     * @return the messageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the senderId
     */
    public Integer getSenderId() {
        return senderId;
    }

    /**
     * @return the fileId
     */
    public byte[] getFileId() {
        return fileId;
    }

    /**
     * @return the chunkNo
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

    @Override
    public String toString() {
        return String.format(
            "Message type: %s\n\tVersion: %s\n\tPeer: %s\n",
            this.messageType.toString(),
            this.version,
            this.senderId);
    }
}