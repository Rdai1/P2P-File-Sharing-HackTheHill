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
    private String peerID; // unique id
    private String IPAddress; // ip address
    private int port; // port number
    private List<Node> peers; // this is the list of peers
    private FileHandling fileHandling; // Instance of FileHandling

    // Constructor
    public Node(String peerID, int port, List<Node> peers) {
        this.peerID = peerID;
        this.port = port;
        this.peers = peers;
        this.IPAddress = getLocalIPAddress(); // Fetch local IP address
        this.fileHandling = new FileHandling(); // Initialize FileHandling instance
    }

    // Method to accept incoming connections and log them.
    public void startListening() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Peer " + peerID + " is listening on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Accepted connection from " + socket.getInetAddress());

            // Handle this new connection in its own thread
            new Thread(() -> {
                try {
                    handleConnection(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // This method handles the incoming connection (calls receive function).
    public void handleConnection(Socket socket) throws IOException {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
            String outputFilePath = "output.txt";
            receive(inputStream, outputFilePath);
            System.out.println("Successfully received, saved to " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error receiving file " + e.getMessage());
        } finally {
            socket.close();
        }
    }

    // This method will handle sending chunks of data to other peers.
    public void send(String targetPeerID, String filePath) {
        List<FileChunk> chunks = fileHandling.chunkFile(filePath); // Use the imported method
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

        // Create a socket connection to the peer's IP address and port number.
        try (Socket socket = new Socket(targetPeer.getIPAddress(), targetPeer.getPort())) {
            // Send the chunks to the peer.
            for (FileChunk chunk : chunks) {
                socket.getOutputStream().write(chunk.getData().length);
                socket.getOutputStream().write(chunk.getData());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the socket connection
        // Add logging
        System.out.println("Sent file to " + targetPeerID);
    }

    // This method will handle receiving chunks of data from other peers.
    public void receive(DataInputStream inputStream, String outputFilePath) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
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
        }

        // Reassemble the file
        //fileHandling.assembleFile(chunks, outputFilePath);
        // Add logging
        System.out.println("Received file from " + inputStream);
        System.out.println("File saved to " + outputFilePath);
    }

    // Basic helper methods
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
