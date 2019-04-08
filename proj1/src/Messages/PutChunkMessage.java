package Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import Utils.Chunk;
import Utils.Hash;

public class PutChunkMessage extends Message {
    private byte[] data; // the raw chunk data

    public PutChunkMessage(String version, Integer senderId, byte[] fileId, Integer chunkNo, Integer replicationDegree,
            byte[] data) {
        super(MessageType.PUTCHUNK, version, senderId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.data = data;
    }

    public PutChunkMessage(String version, Integer senderId, Chunk c) {
        super(MessageType.PUTCHUNK, version, senderId);
        this.fileId = c.getFileID();
        this.chunkNo = c.getChunkNo();
        this.replicationDegree = c.getReplicationDegree();
        this.data = c.getData();
    }

    @Override
    public byte[] getMessage() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        try {
            buf.write(super.createHeader());
            buf.write(this.data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buf.toByteArray();
    }

    @Override
    public String toString() {
        String s = super.toString();
        String data = Hash.getHexHash(this.data);
        s += String.format(
            "\tFileId: %s\n\tChunkNo: %d\n\tReplicationDegree: %d\n\tData: %s...\n\tData size: %d", 
            Hash.getHexHash(this.fileId), 
            this.chunkNo,
            this.replicationDegree,
            data.substring(0, Math.min(15, data.length())),
            this.data.length);
        return s;
    }
}