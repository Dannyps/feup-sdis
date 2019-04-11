package Utils;

import java.io.Serializable;

public class FileInfo implements Serializable {
    // UID for serialization
    private static final long serialVersionUID = -4040935054301086263L;
    // The original filename from whom this chunk belongs to
    String filename;
    // the number of chunks for this file
    Integer chunkNo;
    // the file identifier hash as hexadecimal string
    String fileIdHex;

    /**
     * 
     * @param filename The original file name in the file system
     * @param fileIdHex The hexadecimal file hash string
     * @param chunkNo The amount of chunks needed for this file
     */
    public FileInfo(String filename, String fileIdHex, Integer chunkNo) {
        this.filename = filename;
        this.fileIdHex = fileIdHex;
        this.chunkNo = chunkNo;
    }
}