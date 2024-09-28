import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
        //chunks will be sent one at a time in a loop.
        // Find the target peer in the list of peers.
        Node targetPeer = null;
        for (Node peer : peers) {
            if (peer.getPeerID().equals(targetPeerID)) {
                targetPeer = peer;
                break;
            }
        }
        if (targetPeer == null) {
            System.out.println("Peer " + targetPeerID + " not found.");
            return;
        }

        //create a socket connection to the peers IP address and port number.
        try (Socket socket = new Socket(targetPeer.getIPAddress(), targetPeer.getPort())) {
            //send the chunks to the peer.
            for (byte[] chunk : chunks) {
                socket.getOutputStream().write(chunk.length);
                socket.getOutputStream().write(chunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    //close the socket connection

    //add logging
    System.out.println("Sent file to " + targetPeerID);

    }

    // This method will handle receiving chunks of data from other peers.
    public void receive(DataInputStream inputStream, String outputFilePath) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        // To implement later.
        // Read the chunks from the input stream and add them to the list.
        // Once all chunks are received, reassemble the file.
        try {
            while (true) {
                int chunkSize = inputStream.read();
                if (chunkSize == -1) {
                    break;
                }
                byte[] chunk = new byte[chunkSize];
                inputStream.readFully(chunk);
                chunks.add(chunk);
            }
        } catch (IOException e) {
            e.printStackTrace();

        assembleFile(chunks, outputFilePath);
        //add logging
        System.out.println("Received file from " + inputStream);
        System.out.println("File saved to " + outputFilePath);
    }
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
