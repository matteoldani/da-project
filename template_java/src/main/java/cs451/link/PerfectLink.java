package cs451.link;

import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class PerfectLink extends Link{

    private BlockingQueue<MessagePacket> toSend;
    private BlockingQueue<MessagePacket> toResend;
//    private Set<Integer> acked;
    /**
     * The Integer is the message ID, the byte is the sender ID
     */
    private Set<Map.Entry<Byte, Integer>> acked;

    private DatagramSocket ds;

    private Boolean stop;

    private Function<MessagePacket, Void> deliverMethod;

    public PerfectLink(Function<MessagePacket, Void> deliverMethod){

        // Created the two queues used to handle sending of a message
        this.toSend = new LinkedBlockingQueue<>();
        this.toResend = new LinkedBlockingQueue<>();
        this.acked = new HashSet<>();
        this.stop = false;
        this.deliverMethod = deliverMethod;

        // Starting the socket
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        // Start thread sending and resending
        new Thread(this::send).start();
        new Thread(this::reSend).start();

    }

    /**
     * This method is responsible for the parsing of the received payload.
     * It will rebuild the messages and add them to the delivered list
     * @param msg
     */
    public void deliver(MessagePacket msg){
        this.deliverMethod.apply(msg);
    }

    /**
     * This method is responsible for removing packets for which an ack has
     * been received from the re_send queue.
     *
     * @param ack is the packet containing the ack
     */
    public void receiveAck(AckPacket ack){
        synchronized (acked){
            acked.add(new AbstractMap.SimpleEntry<>(ack.getSenderID(), ack.getPacketID()));
        }
    }

    /**
     * This function will add the packet to the send queue so that it can be
     * sent (eventually)
     * @param packet is the packet to be sent
     */
    public void sendPacket(MessagePacket packet){
        toSend.add(packet);
        System.out.println("Packet add to toSend: " + packet.getPacketID() + " " + packet.getSenderID());
    }

    /**
     * This method is run inside a thread, it will continuously try to get new
     * message from the sending queue. If no messages, the Thread will yield to
     * not slow down the application
     */
    private void send(){

        while(true){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }
            if(toSend.isEmpty()){
                Thread.yield();
            }else{
                try {
                    MessagePacket msgPkt = toSend.take();
                    toResend.add(msgPkt);
                    byte[] msgPayload = msgPkt.serializePacket();
                    DatagramPacket datagramPacket =
                            new DatagramPacket(msgPayload, msgPayload.length,
                                    msgPkt.getIpAddress(), msgPkt.getPort());
                    ds.send(datagramPacket);
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
    private void reSend(){
        while(true){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }
            if(toResend.isEmpty()){
                Thread.yield();
            }else{
                try {
                    MessagePacket msg = toResend.take();
                    synchronized (acked){
                        Map.Entry<Byte, Integer> ack = new AbstractMap.SimpleEntry<>(msg.getSenderID(), msg.getPacketID());
                        if(acked.contains(ack)){
                            // I don't have to re_send it again
                            // I can remove it from the Set
                            acked.remove(ack);
                            continue;
                        }
                    }
                    byte[] msgPayload = msg.serializePacket();
                    DatagramPacket datagramPacket =
                            new DatagramPacket(msgPayload, msgPayload.length,
                                    msg.getIpAddress(), msg.getPort());
                    ds.send(datagramPacket);
                    toResend.add(msg);

                    // I give priorities to the sending thread,
                    // 30 may need to be tuned
                    // TODO
                    // Thread.sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void stopThread(){
        synchronized (this.stop){
            this.stop = true;
        }
    }

}
