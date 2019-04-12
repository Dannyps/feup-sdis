package Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import Utils.Hash;

public abstract class Message {
    /** Required fields */
    protected MessageType messageType;
    protected String version;
    protected Integer senderId;

    /** Optional fields */
    protected byte[] fileId = null;
    protected Integer chunkNo = null;
    protected Integer replicationDegree = null;
    protected byte[] data = null; // the raw chunk data

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

    // #region String to 7-bit ASCII bytes util methods
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
    // #endregion

    /**
     * @return Returns the <CRLF> terminator sequence
     */
    protected byte[] getSequenceTerminator() {
        return new byte[] { '\r', '\n' };
    }

    /**
     * Creates the header following the protocol specification. In addition to the
     * mandatory/common fields between sub-protocols, it considers aditional
     * not-null fields. It assumes the following sequence: <Message type> <Version>
     * <SenderId> <FileId> <ChunkNo> <ReplDegree> <CRLF><CRLF>
     * 
     * @return
     */
    protected byte[] createHeader() {
        // Dynamic byte buffer
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        // Generic format
        // <Message type> <Version> <SenderId> <FileId> <ChunkNo> <ReplDegree>
        // <CRLF><CRLF>

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
            if (this.chunkNo != null) {
                buf.write(this.getChunkNoASCII_());
                buf.write(' ');
            }

            if (this.replicationDegree != null) {
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

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Parses the file id encoded in the messages Each byte represents a single
     * hexadecimal digit, resulting in 64 bytes ID This method reverts to the
     * original 32 bytes SHA-256 format
     * 
     * @param encodedFileId
     * @return
     */
    protected static byte[] parseFileId(byte[] encodedFileId) {
        byte[] originalHash = new byte[32];

        for (int i = 0, j = 0; i < 64; i += 2, j++) {
            char a = (char) encodedFileId[i], b = (char) encodedFileId[i + 1];
            originalHash[j] = (byte) ((a << 4) | (b & 0x0f));
        }
        return originalHash;
    }

    /**
     * Returns the message bytes Note: Only the header is returned. Sub-protocols
     * should override this method to include the body data
     * 
     * @return
     */
    public byte[] getMessage() {
        return this.createHeader();
    }

    /**
     * Parses a datagram packet and returns the corresponding message
     * 
     * @param msg The datagram data
     * @return Message
     * @throws Exception
     */
    public static Message parseMessage(byte[] msg) throws Exception {
        // find the index of the sequence \r\n\r\n
        boolean foundSeq = false;
        int i;
        for (i = 0; i < msg.length - 3 && !foundSeq;) {
            if (msg[i] == '\r' && msg[i + 1] == '\n' && msg[i + 2] == '\r' && msg[i + 3] == '\n')
                foundSeq = true;
            else
                i++;
        }

        // split head and body
        byte[] headRaw = Arrays.copyOf(msg, i - 1);
        byte[] bodyRaw = Arrays.copyOfRange(msg, i + 4, msg.length);

        // Split head in fields
        String[] headFields = new String(headRaw, StandardCharsets.US_ASCII).split(" ");

        // get the message type
        String msgType = headFields[0];

        /** all possible fields */
        String version = headFields[1];
        Integer senderId = Integer.parseInt(headFields[2]);
        byte[] fileId = null;
        Integer chunkNo = null;
        Integer replicationDegree = null;

        if (msgType.equals(MessageType.PUTCHUNK.toString())) {
            fileId = Message.hexStringToByteArray(headFields[3]);
            chunkNo = Integer.parseInt(headFields[4]);
            replicationDegree = Integer.parseInt(headFields[5]);
            return new PutChunkMessage(version, senderId, fileId, chunkNo, replicationDegree, bodyRaw);
        } else if (msgType.equals(MessageType.STORED.toString())) {
            fileId = Message.hexStringToByteArray(headFields[3]);
            chunkNo = Integer.parseInt(headFields[4]);
            return new StoredMessage(version, senderId, fileId, chunkNo);
        } else if (msgType.equals(MessageType.GETCHUNK.toString())) {
            fileId = Message.hexStringToByteArray(headFields[3]);
            chunkNo = Integer.parseInt(headFields[4]);
            return new GetChunkMessage(version, senderId, fileId, chunkNo);
        } else if (msgType.equals(MessageType.CHUNK.toString())) {
            fileId = Message.hexStringToByteArray(headFields[3]);
            chunkNo = Integer.parseInt(headFields[4]);
            return new ChunkMessage(version, senderId, fileId, chunkNo, bodyRaw);
        } else {
            throw new Exception(String.format("Unexpected message type %s", headFields[0]));
        }

    }

    // #region Getters and Setters

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
     * @return the file id as a string in hexadecimal format
     */
    public String getFileIdHexStr() {
        return Hash.getHexHash(this.fileId);
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

    /**
     * @return the message raw data, aka body
     */
    public byte[] getRawData() {
        return data;
    }

    /**
     * @return a DatagramPacket which content is the raw protocol message
     */
    public DatagramPacket getDatagramPacket() {
        byte[] msg = this.getMessage();
        DatagramPacket dp = new DatagramPacket(msg, msg.length);
        return dp;
    }
    // #endregion

    @Override
    public String toString() {
        String s = String.format("Message\n\tType: %s\n\tVersion: %s\n\tSender: %s", this.messageType.toString(),
                this.version, this.senderId);

        // append optional fields
        if (fileId != null)
            s += String.format("\n\tFileId: %s", Hash.getHexHash(this.fileId));

        if (this.chunkNo != null)
            s += String.format("\n\tChunkNo: %d", this.chunkNo);

        if (this.replicationDegree != null)
            s += String.format("\n\tReplicationDegree: %d", this.replicationDegree);

        if (this.data != null) {
            String data = Hash.getHexHash(this.data);
            s += String.format("\n\tData: %s...\n\tData Size: %s", data.substring(0, Math.min(15, data.length())),
                    this.data.length);
        }

        return s;
    }
}