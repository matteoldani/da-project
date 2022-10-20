package cs451.server;

import cs451.Host;
import cs451.link.Link;
import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;
import cs451.packet.PacketType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceiverServer implements Runnable{

    private Link link;
    private DatagramSocket socket;
    private int buff_size;

    private List<Host>  hosts;

    private boolean stop;

    private BlockingQueue<DatagramPacket> to_handle;

    public ReceiverServer(Link link, int buff_Size, int port, List<Host> hosts){
        this.link = link;
        this.buff_size = buff_Size;
        this.stop = false;
        this.hosts = hosts;

        this.to_handle = new LinkedBlockingQueue<>();

        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }


    }

    private void handlePacket(){

        while(!this.stop){
            DatagramPacket packet;
            if(to_handle.isEmpty()){Thread.yield();}
            try {
                packet = to_handle.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            byte[] payload = packet.getData();
//            System.err.println("DEBUG: message payload: ");
//            for(int i=0; i<payload.length;i++){
//                System.out.print(payload[i] + " ");
//            }
//            System.out.println("\nDEBUG: End of packet");

            // check the type of packet
            if(payload[0] == 0){
                // Message Type
                MessagePacket msg = new MessagePacket(payload);
                link.deliver(msg);

                // I need to send an ack
                // I can build the packet by myself, for performance's sake
                byte[] ack_payload = new byte[6];

                // specify the type
                ack_payload[0] = 1;
                // specify the sender ID
                ack_payload[1] = payload[1];
                // specify the Packet_number
                ack_payload[2] = payload[2];
                ack_payload[3] = payload[3];
                ack_payload[4] = payload[4];
                ack_payload[5] = payload[5];

                packet = new DatagramPacket(ack_payload, ack_payload.length,
                        packet.getAddress(), hosts.get(msg.getSender_ID()-1).getPort());

//                System.out.println("DEBUG: sending the ack packet to: " + packet.getPort());
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }else if (payload[0] == 1){
                // ACK type
                AckPacket ack = new AckPacket(payload);
                link.receive_ack(ack);
            }else {
                System.err.println("The message has an invalid type: " + payload[0]);
            }
        }

    }

    @Override
    public void run() {
        new Thread(this::handlePacket).start();
        while(!this.stop){
            byte[] buff = new byte[buff_size];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            to_handle.add(packet);

            // TODO: Check performance if a Thread.yield() is placed here.
        }
    }


    public void stop_thread(){
        this.stop = true;
    }
}
