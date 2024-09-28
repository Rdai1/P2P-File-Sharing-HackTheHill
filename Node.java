import java.io.DataInputStream;
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


    // This method will read from a file (filePath), then divide into chunks of 512 bytes.
    public List<byte[]> chunkFile(String filePath) {
        return new ArrayList<>(); // placeholder code to not get errors.
    }

    // This method will reassemble the previously chunked data back into a complete file.
    private void assembleFile(List<byte[]> chunks, String outputFilePath) throws IOException {

    }

    // This method will handle sending chunks of data to other peers.
    public void send(String targetPeerID, String filePath) {
        List<byte[]> chunks = chunkFile(filePath);
        // To implement later.
    }

    // This method will handle receiving chunks of data from other peers.
    public void receive(DataInputStream inputStream, String outputFilePath) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        // To implement later.

        assembleFile(chunks, outputFilePath);
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
