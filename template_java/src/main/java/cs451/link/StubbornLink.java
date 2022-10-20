package cs451.link;

import cs451.Host;
import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;

import java.io.IOException;
import java.net.*;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class StubbornLink extends Link{

    private BlockingQueue<MessagePacket> to_send;
    private BlockingQueue<MessagePacket> to_resend;
    private Set<Integer> acked;
    /**
     * The Integer is the message ID, the byte is the sender ID
     */
    private Set<Map.Entry<Integer, Byte>> delivered;
    private DatagramSocket ds;

    private boolean stop;

    public StubbornLink(){

        //this.perfectLink = perfectLink;

        // Created the two queues used to handle sending of a message
        this.to_send = new LinkedBlockingQueue<>();
        this.to_resend = new LinkedBlockingQueue<>();
        this.acked = ConcurrentHashMap.newKeySet();
        this.delivered = ConcurrentHashMap.newKeySet();
        this.stop = false;

        // Starting the socket
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        // Start thread sending and resending
        new Thread(this::send).start();

        // DEBUG not starting the resend to better track the messages
        // new Thread(this::re_send).start();

    }

    public void deliver(MessagePacket msg){
        byte sender_ID = msg.getSender_ID();
        byte[] payload = msg.getPayload_b();
        for(int i=7; i<payload.length; i++){
            int msg_len = payload[i];
            int id_message
        }
        Map.Entry<Integer, Byte> e = new AbstractMap.SimpleEntry<>(msg.getPacket_ID(), msg.getSender_ID());
        if(!delivered.contains(e)){
            System.out.println("DEBUG: delivering messages from -> "
                    + msg.getSender_ID()
                    + " with ID -> "
                    + msg.getPacket_ID());
            delivered.add(e);
        }
    }

    /**
     * This method is responsible for removing packets for which an ack has
     * been received from the re_send queue.
     *
     * @param ack is the packet containing the ack
     */
    public void receive_ack(AckPacket ack){
        acked.add(ack.getPacket_ID());
        System.out.println("DEBUG: list of acked packages: ");
        for(Integer id : acked){
            System.out.print(id + " ");
        }
        System.out.println();
    }

    /**
     * This function will add the packet to the send queue so that it can be
     * sent (eventually)
     * @param packet is the packet to be sent
     */
    public void send_packet(MessagePacket packet){
        to_send.add(packet);
    }

    /**
     * This method is run inside a thread, it will continuously try to get new
     * message from the sending queue. If no messages, the Thread will yield to
     * not slow down the application
     */
    private void send(){

        while(!this.stop){
            if(to_send.isEmpty()){
                Thread.yield();
            }else{
                try {
                    MessagePacket msg_pkt = to_send.take();
                    to_resend.add(msg_pkt);
                    byte[] msg_payload = msg_pkt.serializePacket();
                    System.out.println("DEBUG: sending message to port: " + msg_pkt.getPort());
                    System.out.println("DEBUG: message type is -> " + msg_pkt.getType());
                    System.out.println("DEBUG: message payload[0] = " + msg_payload[0]);
                    DatagramPacket dg_pkt =
                            new DatagramPacket(msg_payload, msg_payload.length,
                                    msg_pkt.getIp_address(), msg_pkt.getPort());
                    ds.send(dg_pkt);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * This method is run inside a thread, it will continuously try to get new
     * messages from the re_sending queue. If no messages, the Thread will
     * yield to not slow down the application. Messages are sent with a delay
     * of 30ms to give priorities to the sending queue.
     */
    private void re_send(){
        while(!this.stop){
            if(to_resend.isEmpty()){
                Thread.yield();
            }else{
                try {
                    MessagePacket msg = to_resend.take();
                    if(acked.contains(msg.getPacket_ID())){
                        // I don't have to re_send it again
                        // I can remove it from the Set
                        acked.remove(msg.getPacket_ID());
                        continue;
                    }
                    byte[] msg_payload = msg.serializePacket();
                    DatagramPacket dg_pkt =
                            new DatagramPacket(msg_payload, msg_payload.length,
                                    msg.getIp_address(), msg.getPort());
                    ds.send(dg_pkt);
                    to_resend.add(msg);

                    // I give priorities to the sending thread,
                    // 30 may need to be tuned
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void stop_thread(){
        this.stop = true;
    }

    public Set<Map.Entry<Integer, Byte>> getDelivered() {
        return delivered;
    }
}
