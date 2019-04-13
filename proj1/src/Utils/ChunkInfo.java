package Utils;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Class to hold relevant chunk information
 */
public class ChunkInfo implements Serializable {
    /** UID for serialization */
    private static final long serialVersionUID = -6119751604672763260L;
    /** The desired replication degree as set by the Initiator Peer */
    private Integer replicationDegree;
    /** List of peer identifiers who stored this chunk */
    private TreeSet<Integer> ownerPeers;

    /**
     * 
     * @param replicationDegree
     * @param backupDegree
     */
    public ChunkInfo(Integer replicationDegree) {
        this.replicationDegree = replicationDegree;
        this.ownerPeers = new TreeSet<Integer>();
    }

    /**
     * @return the desired replication degree as defined by the initiator peer
     */
    public Integer getReplicationDegree() {
        return replicationDegree;
    }

    /**
     * @return the backup degree, i.e., the number of peers who backed up this chunk
     *         (including this peer)
     */
    public Integer getBackupDegree() {
        return this.ownerPeers.size() + 1;
    }

    /**
     * Adds a new peer to the list of peers who stored this chunk
     */
    public void addOwnerPeer(Integer peerId) {
        this.ownerPeers.add(peerId);
    }

    /**
     * Removes a peer from the owners of this chunk
     */
    public void removeOwnerPeer(Integer peerId) {
        this.ownerPeers.remove(peerId);
    }

}