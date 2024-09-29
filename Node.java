import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.io.EOFException;
import java.io.File;

public class Node {
    // attributes
    private String peerID; // unique id
    private String IPAddress; // ip address
    private int port; // port number
    private List<Node> peers; // this is the list of peers
    private FileHandling fileHandling; // Instance of FileHandling

    // Constructor
    public Node(String peerID) {
        this.peerID = peerID;
        // this.port = port;
        this.peers = new ArrayList<Node>();
        this.IPAddress = getLocalIPAddress(); // Fetch local IP address
        this.fileHandling = new FileHandling(); // Initialize FileHandling instance
    }

    public Node(String peerID, int port, List<Node> peers) {
        this.peerID = peerID;
        this.port = port;
        this.peers = peers;
        this.IPAddress = getLocalIPAddress(); // Fetch local IP address
        this.fileHandling = new FileHandling(); // Initialize FileHandling instance
    }

    public void discoverPeer() throws IOException{
        for (int p = 5000; p < 5010; p++){
            if (p == port){continue;}
            try (Socket socket = new Socket()){
                socket.connect(new InetSocketAddress(IPAddress, p), 2000);
                System.out.println("Attempting to connect to on port " + port);

                System.out.println("FOUND A FRIEND");

                break;
            } catch (SocketTimeoutException e) {
                System.out.println("Port " + port + " timed out.");
            } catch (IOException e) {
                System.out.println("No client found on port " + port + ".");
            }
        }
    }

    // Method to accept incoming connections and log them.
    public void startListening() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        boolean flag = false;

        for (int p = 5000; p < 5010; p++){
            try{
                serverSocket = new ServerSocket(p);
                System.out.println("Assigned client to port: " + port);
                flag = true;
                break;
            }catch (IOException e) {
                System.out.println("Port " + port + " is already in use. Trying next...");
            }
        }

        System.out.println("IP ADDRESS: " + IPAddress);

        System.out.println("Peer " + peerID + " is listening on port " + port);

        discoverPeer();
        
        while (flag) {
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
            String command = inputStream.readUTF();
            if (command.equals("Send")){
                String outputFilePath = "output.txt";
                receive(inputStream, outputFilePath);
                System.out.println("Successfully received, saved to " + outputFilePath);
            }else if (command.equals("getPeers")){

            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error receiving file " + e.getMessage());
        } finally {
            socket.close();
        }
    }

    public void sendPeer(){
        
    }

    // This method will handle sending chunks of data to other peers.
    public void send(String targetPeerID, String filePath) {
        List<FileChunk> chunks = fileHandling.chunkFile(filePath);
        File file = new File(filePath);

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
        try (Socket socket = new Socket(targetPeer.getIPAddress(), targetPeer.getPort());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            // Send file name and size
            outputStream.writeUTF("Send");
            outputStream.writeUTF(file.getName());
            outputStream.writeLong(file.length()); // Send file size
            outputStream.writeInt(chunks.size());

            // Calculate the hash of the file
            String fileHash = HashUtil.generateFileHash(file); // Implement this method to use SHA-256

            // Send the chunks to the peer.
            for (FileChunk chunk : chunks) {
                outputStream.writeInt(chunk.getData().length); // Send chunk size as an integer
                outputStream.write(chunk.getData());           // Send chunk data
            }

            // Send the hash after sending all chunks
            outputStream.writeUTF(fileHash);
            System.out.println("Sent file hash: " + fileHash);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Close the socket connection
        System.out.println("Sent file to " + targetPeerID);
    }

    // This method will handle receiving chunks of data from other peers.
    public void receive(DataInputStream inputStream, String outputFilePath) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        String fileName = "received_" + inputStream.readUTF(); // Receive the file name
        long fileSize = inputStream.readLong(); // Receive the expected file size

        try {

            // Read the expected number of chunks
            int chunkCount = inputStream.readInt();

            // Read all the expected chunks
            for (int i = 0; i < chunkCount; i++) {
                int chunkSize = inputStream.readInt(); // Read chunk size as an integer
                byte[] chunk = new byte[chunkSize];
                inputStream.readFully(chunk);
                chunks.add(chunk);
            }

            System.out.println("FINISHED WHILE LOOP");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reassemble the file
        fileHandling.assembleFile(chunks, fileName);
        System.out.println("Received file from " + inputStream);
        System.out.println("File saved to " + fileName);

        // Now, receive the file hash
        String receivedHash = inputStream.readUTF();
        System.out.println("Received file hash: " + receivedHash);

        // Verify the integrity of the received file
        try {
            // Check the file size
            if (new File(fileName).length() != fileSize) {
                System.out.println("File size does not match. Possible corruption.");
                return;
            }

            // Calculate the SHA-256 hash of the received file
            String calculatedHash = HashUtil.generateFileHash(new File(fileName));
            if (receivedHash.equals(calculatedHash)) {
                System.out.println("File integrity verified.");
            } else {
                System.out.println("File integrity check failed. The file may be corrupted.");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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

    public static void main(String[] args) {
        if (args.length >= 1){
            Node peer = new Node(args[0]);

            new Thread(() -> {
                try {
                    peer.startListening();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        
    }
}
