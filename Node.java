import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

    //Method to accept incoming connections and log them.
    public void startListening() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Peer " + peerID + " is listening on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Accepted connection from " + socket.getInetAddress());
            // to be continued later.
        }
    }


    // This method will typically read from a file (filePath), then divide into chunks of 512 bytes.
    public List<byte[]> chunkFile(String filePath) {
        return new ArrayList<>(); // placeholder code to not get errors.
    }


    //basic helper methods
    public void addPeer(Node peer) {
        peers.add(peer);
    }

    public int getPort() {
        return port;
    }

    public String getPeerID() {
        return peerID;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public List<Node> getPeers() {
        return peers;
    }

}
