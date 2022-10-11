package cs451.server;

import cs451.link.Link;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ReceiverServer implements Runnable{

    private Link link;
    private DatagramSocket socket;
    private byte[] buff;

    public ReceiverServer(Link link, int buff_Size, int port){
        this.link = link;
        this.buff = new byte[buff_Size];

        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }


    }

    private void handlePacket(DatagramPacket packet){

        byte[] payload = packet.getData();

        // check the type of packet
        

    }

    @Override
    public void run() {
        while(true){
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            packet = new DatagramPacket(buff, buff.length, address, port);
            String received
                    = new String(packet.getData(), 0, packet.getLength());


            try {
                socket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
