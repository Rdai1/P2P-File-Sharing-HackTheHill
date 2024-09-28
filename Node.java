import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Node {
    // attributes
    private String peerID; //unique id
    private String IPAddress; //ip address
    private int port; //port numebr
    private List<Node> peers; // this is the list of peers

    // Constructor
    public Node(String peerID, int port, List<Node> peers) {
        this.peerID = peerID;
        this.port = port;
        this.peers = peers;
        this.IPAddress = getLocalIPAddress(); // Fetch local IP address
    }

    //Method to accept incoming connections and log them.
    public void startListening() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Peer " + peerID + " is listening on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Accepted connection from " + socket.getInetAddress());

            // We want to handle this new connection in its own thread so that we are able to handle multiple connections at teh same time.
            new Thread(() -> {
                try {
                    handleConnection(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }


    //to do later... this method handles the incoming connection. (calls receive function).
    public void handleConnection(Socket socket) throws IOException{

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
    private String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "0.0.0.0";
        }
    }

}
