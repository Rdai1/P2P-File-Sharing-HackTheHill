import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        //test sending here?
    }
}
