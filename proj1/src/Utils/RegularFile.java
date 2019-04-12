package Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RegularFile {
    private static int CHUNK_MAX_SIZE = 64000;
    /** maximum file size chunk */
    private byte[] fileID = null;
    /** file identifier sha-256 hash */
    private String path;
    /** the path to the file */
    private int replicationDegree;
    /** desired file replication */

    /**
     * File relevant attributes
     */
    private String fileName;
    /** the filename (retrieved from @see RegularFile.path ) */
    private long fileSize;
    /** the file size in bytes */
    private FileTime fileCreationTime;
    /** the file creation time */
    private FileTime fileLastModificationTime;
    /** file last modified time */
    private String fileOwner;

    /** file ownwer */

    /**
     * 
     * @param pathname
     * @param replicationDegree
     */
    public RegularFile(String pathname, int replicationDegree) {
        this.replicationDegree = replicationDegree;
        this.path = pathname;
    }

    /**
     * @return the fileID
     */
    public byte[] getFileID() {
        return fileID;
    }

    /**
     * Loads the required file attributes
     * 
     * @throws IOException
     */
    private void loadFileAttributes() throws IOException {
        Path p = Paths.get(this.path);

        BasicFileAttributes fileAttrs = Files.readAttributes(p, BasicFileAttributes.class);

        this.fileName = p.getFileName().toString();
        this.fileCreationTime = fileAttrs.creationTime();
        this.fileLastModificationTime = fileAttrs.lastModifiedTime();
        this.fileSize = fileAttrs.size();
        this.fileOwner = Files.getOwner(p).toString();
    }

    /**
     * Computes an indentifier for a regular file based on filename, creation time,
     * last modification time and size
     * 
     * @throws IOException Specified file does not exist or other I/O issues
     */
    private void getFileHash() throws IOException {
        try {
            // Get file metadata (creation time, modification time, owner, filename)
            this.loadFileAttributes();

            // Setup digest algorithm
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Build string to be hashed
            String preHash = this.fileName + this.fileCreationTime.toString() + this.fileLastModificationTime.toString()
                    + this.fileOwner + this.fileSize;

            // Compute hash
            this.fileID = md.digest(preHash.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            // TODO
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @brief Splits the file in chunks
     * @return A list of the created chunks
     * @throws IOException
     */
    public ArrayList<Chunk> getChunks() throws IOException {
        // compute the hash for the file
        this.getFileHash();

        // Initialize list of Chunks
        ArrayList<Chunk> chunks = new ArrayList<>();

        // Auxiliar variables to handle the file reading offset
        int chunkNum = 0;
        long remainingSize = this.fileSize;

        try {
            // Open the file
            FileInputStream file = new FileInputStream(this.path);
            int ret = 0;

            // Create chunks of CHUNK_MAX_SIZE
            // Aditional iteration is for the reminder chunk of size < CHUNK_MAX_SIZE
            for (int i = 0; i < this.fileSize / RegularFile.CHUNK_MAX_SIZE + 1; i++) {
                int sizeToRead;
                if (remainingSize >= RegularFile.CHUNK_MAX_SIZE)
                    sizeToRead = RegularFile.CHUNK_MAX_SIZE;
                else
                    sizeToRead = (int) remainingSize; // safe cast because last chunk is < CHUNK_MAX_SIZE (64k)

                // allocate buffer
                byte[] b = new byte[sizeToRead];

                // read a chunk of data
                ret = file.read(b, 0, sizeToRead);

                // Check for read failure (unexpected EOF)
                if (ret == -1) {
                    file.close();
                    throw new Exception("Ops"); // TODO
                }

                // Update remaining file size to read
                remainingSize -= ret;

                // create chunk object
                Chunk chunk = new Chunk(this.fileID, chunkNum, this.replicationDegree, b);
                chunks.add(chunk);

                // update chunk number
                chunkNum++;

            }

            // Close file
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chunks;
    }

    public byte[] getFileId() throws IOException {
        if (this.fileID == null)
            getFileHash();
        return this.fileID;
    }

    public String getFileIdHexStr() {
        if (this.fileID == null)
            try {
                getFileHash();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        return Hash.getHexHash(this.fileID);
    }

    /**
     * @return the replicationDegree
     */
    public int getReplicationDegree() {
        return replicationDegree;
    }
}