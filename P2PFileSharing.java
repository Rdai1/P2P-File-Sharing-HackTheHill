import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class P2PFileSharing {

    //simple implementation of MAIN CLASS (orchestrator)
    public static void main(String[] args) {
        List<Node> peers = new ArrayList<>();

        Node peer1 = new Node("Peer1", 5001, peers);
        Node peer2 = new Node("Peer2", 5002, peers);
        Node peer3 = new Node("Peer3", 5003, peers);
        Node peer4 = new Node("Peer4", 5004, peers);

        // Add peers to the list
        peers.add(peer1);
        peers.add(peer2);
        peers.add(peer3);
        peers.add(peer4);

        // Start listening for connections in separate threads
        new Thread(() -> {
            try {
                peer1.startListening();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                peer2.startListening();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                peer3.startListening();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                peer4.startListening();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        //first we've got to make a command line interface to interact with the peers
        System.out.println("Welcome to the P2P File Sharing System!");
        System.out.println("Commands are found below:");
        System.out.println("1. List all peers");
        System.out.println("2. Send a file to a peer");
        System.out.println("3. Exit");
        // now we gotta take the input from the user
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running){
            System.out.println("Note: you are currently using " + peer1.getPeerID());
            System.out.println("Enter a command:");
            int command = scanner.nextInt();
            switch (command){
                case 1:
                    System.out.println("List of all peers:");
                    for (Node peer : peers){
                        System.out.println(peer.getPeerID());
                    }
                    break;
                case 2:
                    System.out.println("Enter the name of the peer you want to send the file to:");
                    String targetPeerID = scanner.next();
                    if (targetPeerID.equals(peer1.getPeerID())){
                        System.out.println("You cannot send a file to yourself.");
                        break;
                    }
                    if (peers.stream().noneMatch(peer -> peer.getPeerID().equals(targetPeerID))){
                        System.out.println("Peer " + targetPeerID + " not found.");
                        break;
                    }
                    System.out.println("Enter the path of the file you want to send:");
                    String filePath = scanner.next();
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            peer1.send(targetPeerID, filePath);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
            }
        }
        



        // // Test sending a file from Peer1 to Peer2
        // String filePath = "path/to/your/testfile.txt"; // Replace with the actual path of the test file
        // new Thread(() -> {
        //     try {
        //         // Allow some time for peers to start listening
        //         Thread.sleep(2000);
        //         peer1.send("Peer2", filePath);
        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        // }).start();
    }
}
