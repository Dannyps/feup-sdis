package Utils;

import java.io.Serializable;

/**
 * Class to hold relevant chunk information
 */
public class ChunkInfo implements Serializable {
    // UID for serialization
    private static final long serialVersionUID = -6119751604672763260L;
    // the desired replication degree
    private Integer replicationDegree;
    // the current replication degree
    private Integer backupDegree;
    
    /**
     * 
     */
    public ChunkInfo() {
        this.replicationDegree = 0;
        this.backupDegree = 0;
    }

    /**
     * 
     * @param replicationDegree
     * @param backupDegree
     */
    public ChunkInfo(Integer replicationDegree, Integer backupDegree) {
        this.replicationDegree = replicationDegree;
        this.backupDegree = backupDegree;
    }

    /**
     * @return the replicationDegree
     */
    public Integer getReplicationDegree() {
        return replicationDegree;
    }

    /**
     * @return the backupDegree
     */
    public Integer getBackupDegree() {
        return backupDegree;
    }

    /**
     * @param backupDegree the backupDegree to set
     */
    public void setBackupDegree(Integer backupDegree) {
        this.backupDegree = backupDegree;
    }

    /**
     * Increase the backup degree
     */
    public void increaseBackupDegree() {
        this.backupDegree++;
    }

    /**
     * Decrease the backup degree
     */
    public void decreaseBackupDegree() {
        this.backupDegree--;
    }
}