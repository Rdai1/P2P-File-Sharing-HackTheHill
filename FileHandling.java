import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;

public class FileHandling {
    private static final int CHUNK_SIZE = 512;
    // This method will read from a file (filePath), then divide into chunks of 512 bytes.
    public List<FileChunk> chunkFile(String filePath) {
        
        List<FileChunk> chunks = new ArrayList<>();

        try(FileInputStream inputFile = new FileInputStream(filePath)){
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            
            int chunkNumber = 0;
            while ((bytesRead = inputFile.read(buffer)) != -1){
                byte[] data = new byte[bytesRead];
                System.arraycopy(buffer, 0, data, 0, bytesRead);
                chunks.add(new FileChunk(chunkNumber++, data));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Chunk size = " + chunks.size());
        return chunks;
    }


    // This method will reassemble the previously chunked data back into a complete file.
    public void assembleFile(List<byte[]> chunks, String outputFilePath) throws IOException {
        try (FileOutputStream outputFile = new FileOutputStream(outputFilePath)) {
            for (byte[] chunk : chunks) {
                outputFile.write(chunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
