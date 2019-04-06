import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.nio.file.Paths;
import java.util.ArrayList;

class RegularFile {
    private static int CHUNK_MAX_SIZE = 64000; /** The maximum size by chunk */
    private String fileID;
    private String filename;
    private int replicationDegree;
    private long fileSize = 0;
    RegularFile(String filename, int replicationDegree) {
        this.replicationDegree = replicationDegree;
        this.filename = filename;
    }
    /**
     * @brief Computes an indentifier for a regular file based on filename, creation time, last modification time and size
     * @see RegularFile.fileID
     */
    private void getFileHash() {
        // TODO
        // Get the absolute current path (relative paths are not working, this might not be the best approach)
        //String absolutePath = Paths.get(".").toAbsolutePath().normalize().toString();

        // Get file metadata (creation time, modification time, owner, filename)
        try {
            // Get file attributes
            Path p = Paths.get(this.filename);
            System.out.println("Got path: " + p.toString());
            BasicFileAttributes fileAttrs = Files.readAttributes(p, BasicFileAttributes.class);
            // Compute hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String preHash = this.filename + fileAttrs.creationTime() + fileAttrs.lastModifiedTime() + fileAttrs.size() + 
                Files.getOwner(Paths.get(this.filename)).toString();
            this.fileID = md.digest(preHash.getBytes()).toString();
            // update internal reference for file size
            this.fileSize = fileAttrs.size();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @brief Splits the file in chunks
     * @return A list of the created chunks
     */
    ArrayList<Chunk> getChunks() {
        // compute the hash for the file
        this.getFileHash();

        // Initialize list of Chunks
        ArrayList<Chunk> chunks = new ArrayList<>();

        // Auxiliar variables to handle the file reading offset
        int chunkNum = 0;
        long remainingSize = this.fileSize;

        try {
            // Open the file
            FileInputStream file = new FileInputStream(this.filename);
            int ret = 0;

            // Create chunks of CHUNK_MAX_SIZE
            // Aditional iteration is for the reminder chunk of size < CHUNK_MAX_SIZE
            for(int i = 0; i < this.fileSize/RegularFile.CHUNK_MAX_SIZE + 1; i++) {
                int sizeToRead;
                if(remainingSize > RegularFile.CHUNK_MAX_SIZE)
                    sizeToRead = RegularFile.CHUNK_MAX_SIZE;
                else
                    sizeToRead = (int)remainingSize; // safe cast because last chunk is < CHUNK_MAX_SIZE (64k)
                
                // allocate buffer
                byte[] b = new byte[sizeToRead];
                
                // read a chunk of data
                ret = file.read(b, 0, sizeToRead);
                
                // Check for read failure (unexpected EOF)
                if(ret == -1)
                    throw new Exception("Ops"); // TODO
                
                // Update remaining file size to read
                remainingSize -= ret;
                
                // create chunk object
                Chunk chunk = new Chunk(this.fileID, chunkNum, this.replicationDegree, b);
                chunks.add(chunk);
                    
                // update chunk number
                chunkNum++;

                // Close file
                file.close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return chunks;
    }
}