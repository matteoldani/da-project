package cs451.link;

import cs451.Host;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class StubbornLink {

    BlockingQueue to_send;
    InetAddress ip;
    int port;
    DatagramSocket ds;

    public StubbornLink(Host host){

        this.to_send = new LinkedBlockingQueue();
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(){

    }


}
