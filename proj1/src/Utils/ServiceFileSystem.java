package Utils;

import Shared.Peer;

/**
 * Collection of static methods that tell the specific location for storing files and chunks data
 */
public class ServiceFileSystem {
    /**
     * @param fileId The file id hash string in hexadecimal representation
     * @return The relative path to the backup file directory
     */
    public static String getBackupFilePath(String fileId) {
        return String.format("peer%d/backup/%s", Peer.getInstance().getPeerId(), fileId);
    }

    /**
     * @param fileId The file id hash string in hexadecimal representation
     * @param chunkNo The chunk number
     * @return The relative path to the backup chunk of the file fileId
     */
    public static String getBackupChunkPath(String fileId, Integer chunkNo) {
        return String.format("peer%d/backup/%s/chk%d", Peer.getInstance().getPeerId(), fileId, chunkNo);
    }

    /**
     * @param fileId The file id hash string in hexadecimal representation
     * @return The relative path to the restored file directory
     */
    public static String getRestoredFilePath(String fileId) {
        return String.format("peer%d/restore/%s", Peer.getInstance().getPeerId(), fileId);
    }

    /**
     * @param fileId The file id hash string in hexadecimal representation
     * @param chunkNo The chunk number
     * @return The relative path to the restored chunk of the file fileId
     */
    @Deprecated // restoring chunks is not advised
    public static String getRestoredChunkPath(String fileId, Integer chunkNo) {
        return String.format("peer%d/restore/%s_chunks/chk%d", Peer.getInstance().getPeerId(), fileId, chunkNo);
    }
}