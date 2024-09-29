import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Scanner;

public class Node {
    // attributes
    private String peerID; // unique id
    private String IPAddress; // ip address
    private int port; // port number
    // private List<Node> peers; // this is the list of peers
    private List<Info> peers;
    private FileHandling fileHandling; // Instance of FileHandling

    // Constructor
    public Node(String peerID) {
        this.peerID = peerID;
        // this.port = port;
        this.peers = new ArrayList<Info>();
        this.IPAddress = getLocalIPAddress(); // Fetch local IP address
        this.fileHandling = new FileHandling(); // Initialize FileHandling instance
    }

    public Node(String peerID, int port, List<Node> peers) {
        this.peerID = peerID;
        this.port = port;
        // this.peers = peers;
        this.IPAddress = getLocalIPAddress(); // Fetch local IP address
        this.fileHandling = new FileHandling(); // Initialize FileHandling instance
    }

    @SuppressWarnings("unchecked")
    public void discoverPeer() throws IOException {
        for (int p = 5000; p < 5005; p++) {
            if (p == port) {
                continue;
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(IPAddress, p), 2000);
                System.out.println("Attempting to connect to on port " + p);

                try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                    outputStream.writeUTF("getPeers");
                    outputStream.writeUTF(peerID);

                    try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
                        peers = (List<Info>) inputStream.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    System.out.println(peers.size());
                    if (peers.size() > 0) {
                        for (Info i : peers) {
                            System.out.println(i.peerID);
                        }
                    }
                }
                break;
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                System.out.println("No client found on port " + port + ".");
            }
        }
    }

    // Method to accept incoming connections and log them.
    public void startListening() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        boolean flag = false;

        for (int p = 5000; p <= 5010; p++) {
            try {
                serverSocket = new ServerSocket(p);
                port = p;
                System.out.println("Assigned client to port: " + port);
                flag = true;
                break;
            } catch (IOException e) {
                System.out.println("Port " + port + " is already in use. Trying next...");
            }
        }


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
            if (command.equals("receive")) {
                String outputFilePath = "output.txt";
                receive(inputStream, outputFilePath);
                System.out.println("Successfully received, saved to " + outputFilePath);
            } else if (command.equals("send")) {
                String filePath = inputStream.readUTF();
                send(socket.getInetAddress().getHostAddress(), filePath);
                System.out.println("File uploaded.");
            } else if (command.equals("getPeers")) {
                String from = inputStream.readUTF();
                sendPeers(socket, from);
            }else if (command.equals("remove")){
                removePeer(inputStream.readUTF());
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error receiving file " + e.getMessage());
        } finally {
            socket.close();
        }
    }

    public void sendPeers(Socket s, String from) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream())) {
            List<Info> allPeers = new ArrayList<Info>();
            for (Info p : peers) {
                allPeers.add(new Info(p.peerID, p.IPAddress, p.port));
            }
            allPeers.add(new Info(peerID, IPAddress, port));
            out.writeObject(allPeers);
            out.flush();
            peers.add(new Info(from, s.getInetAddress().getLocalHost().getHostAddress(), s.getPort()));
        }
    }

    // This method will handle sending chunks of data to other peers.
    public void send(String targetPeerID, String filePath) {
        List<FileChunk> chunks = fileHandling.chunkFile(filePath);
        File file = new File(filePath);

        // Find the target peer in the list of peers.
        Info targetPeer = null;
        for (Info peer : peers) {
            if (peer.peerID.equals(targetPeerID)) {
                targetPeer = peer;
                break;
            }
        }
        if (targetPeer == null) {
            System.out.println("Peer " + targetPeerID + " not found.");
            return;
        }

        // Create a socket connection to the peer's IP address and port number.
        try (Socket socket = new Socket(targetPeer.IPAddress, targetPeer.port);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            // Send file name and size
            outputStream.writeUTF("receive");
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
            // System.out.println("Sent file hash: " + fileHash);
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

            System.out.println("In try");
            // Read the expected number of chunks
            int chunkCount = inputStream.readInt();

            // Read all the expected chunks
            for (int i = 0; i < chunkCount; i++) {
                int chunkSize = inputStream.readInt(); // Read chunk size as an integer
                byte[] chunk = new byte[chunkSize];
                inputStream.readFully(chunk);
                chunks.add(chunk);
            }

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

    public void terminateConnection() throws IOException{
        for (Info p: peers){
            try (Socket socket = new Socket(p.IPAddress, p.port)){
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF("remove");
                out.writeUTF(peerID);
            }
            
        }
    }

    // Basic helper methods
    public void addPeer(String id, String ip, int port) {
        peers.add(new Info(id, ip, port));
    }

    public void removePeer(String id){
        for (Info p: peers){
            if (p.peerID.equals(id)){
                peers.remove(p);
                break;
            }
        }
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

    public List<Info> getPeers() {
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
        if (args.length < 1) {
            // System.out.println("Usage: java Node <peerID>");
            return;
        }

        Node peer = new Node(args[0]);

        new Thread(() -> {
            try {
                peer.startListening();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\nPeer ID: " + peer.getPeerID());
            System.out.println("1. List all peers");
            System.out.println("2. Send a file");
            System.out.println("3. Exit");

            int command = scanner.nextInt();
            switch (command) {
                case 1:
                    System.out.println("Peers:");
                    for (Info p : peer.getPeers()) {
                        System.out.println("ID: " + p.peerID + ", IP: " + p.IPAddress + ", Port: " + p.port);
                    }
                    break;

                case 2:
                    System.out.println("Enter peer ID to send file:");
                    String targetPeerID = scanner.next();
                    System.out.println("Enter file path:");
                    String filePath = scanner.next();
                    new Thread(() -> peer.send(targetPeerID, filePath)).start();
                    break;

                case 3:
                    running = false;
                    try {
                        peer.terminateConnection();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;

                default:
                    System.out.println("Invalid command.");
            }
        }

        scanner.close();
    }
}