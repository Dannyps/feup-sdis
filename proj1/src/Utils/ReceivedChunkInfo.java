package Utils;

/**
 * ReceivedChunkInfo
 */
public class ReceivedChunkInfo {

    private String fileIdHexStr;
    private int chunkNo;
    private long receivedAt;

    public ReceivedChunkInfo(String fileId, int cno, long receivedAt){
        this.fileIdHexStr=fileId; 
        this.chunkNo=cno;
        this.receivedAt = receivedAt;
    }
}