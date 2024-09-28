import java.util.ArrayList;
import java.util.List;

public class Node {
    // attributes
    private String peerID;
    private String IPAddress;
    private int port;
    private List<Node> peers; // this is the list of peers

    // Constructor
    public Node(String peerId, String ipAddress, int port, List<Node> peers) {
        this.peerID = peerId;
        this.IPAddress = ipAddress;
        this.port = port;
        this.peers = peers;
    }

    // This method will typically read from a file (filePath), then divide into chunks of 512 bytes.
    public List<byte[]> chunkFile(String filePath) {
        return new ArrayList<>(); // placeholder code to not get errors.
    }
}
