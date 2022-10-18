package cs451.server;

import cs451.link.Link;
import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;
import cs451.packet.PacketType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ReceiverServer implements Runnable{

    private Link link;
    private DatagramSocket socket;
    private byte[] buff;

    private boolean stop;

    public ReceiverServer(Link link, int buff_Size, int port){
        this.link = link;
        this.buff = new byte[buff_Size];
        this.stop = false;

        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }


    }

    private void handlePacket(DatagramPacket packet){

        byte[] payload = packet.getData();

        // check the type of packet
        if(payload[0] == 0){
            // Message Type
            MessagePacket msg = new MessagePacket(payload);
            link.deliver(msg);

            // I need to send an ack
            // I can build the packet by myself, for performance sake
            byte[] ack_payload = new byte[3];

            // specify the type
            ack_payload[0] = 1;
            // specify the sender ID
            ack_payload[1] = payload[1];
            // specify the Packet_number
            ack_payload[2] = payload[2];

            packet = new DatagramPacket(ack_payload, ack_payload.length,
                    packet.getAddress(), packet.getPort());
            try {
                socket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }else if (payload[0] == 1){
            // ACK type
            AckPacket ack = new AckPacket(payload[1], payload[2]);
            link.receive_ack(ack);

        }else {
            System.err.println("The message has an invalid type");
        }

    }

    @Override
    public void run() {
        while(!this.stop){
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            handlePacket(packet);

            // TODO: Check performance if a Thread.yield() is placed here.
        }
    }


    public void stop_thread(){
        this.stop = true;
    }
}
