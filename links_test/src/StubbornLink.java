import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StubbornLink {

    BlockingQueue<byte[]> to_send;
    DatagramSocket ds;

    public StubbornLink(){
        this.to_send = new LinkedBlockingQueue<byte[]>();
        try {
            this.ds = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void addMessage(byte[] payload){
        to_send.add(payload);
    }

    public void removeMessage()



}
