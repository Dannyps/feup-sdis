package Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import utils.Chunk;

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
}