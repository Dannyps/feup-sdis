package Workers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import Utils.Hash;
import Utils.PrintMessage;
import Utils.ServiceFileSystem;

public class DeleteWorker implements Runnable {
    private byte[] fileId;

    public DeleteWorker(byte[] fileId) {
        this.fileId = fileId;
    }

    /**
     * Method to handle the deletion of a non-empty folder Source:
     * https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
     * 
     * @param path
     * @throws IOException
     */
    private void deleteDirectory(Path path) throws IOException {
        // iterate through directories which are not hard/soft links
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    this.deleteDirectory(entry);
                }
            }
        }
        // if is not a directory but a regular file, delete it
        Files.delete(path);
    }

    @Override
    public void run() {
        String fileIdHex = Hash.getHexHash(this.fileId);
        try {
            if (new File(ServiceFileSystem.getBackupFilePath(fileIdHex)).exists()) {
                Path p = Paths.get(ServiceFileSystem.getBackupFilePath(fileIdHex));
                this.deleteDirectory(p);
                PrintMessage.p("DELETE FILE", "Deleted all chunks for file " + fileIdHex);
            }
        } catch (InvalidPathException e) {
            // the Paths.get failed
            e.printStackTrace();
            return;
        } catch (Exception e) {
            // something went wrong in deleteDirectory
            e.printStackTrace();
            return;
        }

    }
}