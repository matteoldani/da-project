package cs451.server;

import cs451.Host;
import cs451.link.Link;
import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;
import cs451.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceiverServer implements Runnable{

    private Link link;
    private DatagramSocket socket;
    private int buffSize;

    private List<Host>  hosts;

    private Boolean stop;
    private byte[] port;
    private BlockingQueue<DatagramPacket> toHandle;

    public ReceiverServer(Link link, int buffSize, int port, List<Host> hosts){
        this.link = link;
        this.buffSize = buffSize;
        this.stop = false;
        this.hosts = hosts;
        this.port = Utils.fromIntToBytes(port);

        this.toHandle = new LinkedBlockingQueue<>();

        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }


    }

    private void handlePacket(){

        while(true){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }
            DatagramPacket packet;
            try {
                packet = toHandle.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            byte[] payload = packet.getData();

            // check the type of packet
            if(payload[0] == 0){
                // ProposalMessage Type
                MessagePacket msg = new MessagePacket(payload);
                link.deliver(msg);

                // I need to send an ack
                // I can build the packet by myself, for performance's sake
                byte[] ackPayload = new byte[11];

                // specify the type
                ackPayload[0] = 1;
                // specify the sender ID
                ackPayload[1] = payload[1];
                // specify the original sender ID
                ackPayload[2] = payload[2];
                // specify packet number
                ackPayload[3] = payload[3];
                ackPayload[4] = payload[4];
                ackPayload[5] = payload[5];
                ackPayload[6] = payload[6];
                // specify the port to which the message was sent
                ackPayload[7]  = this.port[0];
                ackPayload[8]  = this.port[1];
                ackPayload[9]  = this.port[2];
                ackPayload[10] = this.port[3];

                packet = new DatagramPacket(ackPayload, ackPayload.length,
                        packet.getAddress(), hosts.get(msg.getSenderID()-1).getPort());

                try {
                    socket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }else if (payload[0] == 1){
                // ACK type
                AckPacket ack = new AckPacket(payload);
                link.receiveAck(ack);
            }else {
                System.err.println("The message has an invalid type: " + payload[0]);
            }
        }

    }

    @Override
    public void run() {
        new Thread(this::handlePacket).start();
        while(true){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }
            byte[] buff = new byte[buffSize];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            toHandle.add(packet);
        }
    }


    public void stopThread(){
        synchronized (this.stop){
            this.stop = true;
        }
    }
}
