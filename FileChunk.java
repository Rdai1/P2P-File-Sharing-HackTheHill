public class FileChunk {
    private int chunkNumber;
    private byte[] data;

    public FileChunk(int chunkNumber, byte[] data){
        this.chunkNumber = chunkNumber;
        this.data = data;
    }

    public int getChunkNumber(){
        return chunkNumber;
    }

    public byte[] getData(){
        return data;
    }
}
