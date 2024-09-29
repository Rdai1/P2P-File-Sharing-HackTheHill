import java.io.Serializable;

public class Info implements Serializable{
    private static final long serialVersionUID = 1L;
    public String peerID;
    public String IPAddress;
    public int port;

    public Info(String peerID, String ip, int port){
        this.peerID = peerID;
        this.IPAddress = ip;
        this.port = port;
    }
}
