package cs451.link;

import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;
import cs451.utils.Triplet;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class PerfectLink extends Link{

    private PriorityBlockingQueue<Map.Entry<MessagePacket, Integer>> toSend;
    private Set<AckPacket> acked;
    private Set<Triplet> delivered;
    private DatagramSocket ds;
    private Boolean stop;
    private Function<MessagePacket, Void> deliverMethod;
    private Function<Void, Boolean> askForPackets;
    private Map<Byte, Integer> maxSequenceNumberDelivered;

    private Integer msgAcked;

    private class MyComp implements Comparator<Map.Entry<MessagePacket, Integer>>{
        @Override
        public int compare(Map.Entry<MessagePacket, Integer> messagePacketIntegerEntry, Map.Entry<MessagePacket, Integer> t1) {
            return Integer.compare(messagePacketIntegerEntry.getValue(), t1.getValue());
        }
    }

    public PerfectLink(Function<MessagePacket, Void> deliverMethod){

        // Created the two queues used to handle sending of a message
        MyComp myComparator = new MyComp();
        this.toSend = new PriorityBlockingQueue<>(1000, myComparator);
        this.acked = new HashSet<>();
        this.stop = false;
        this.deliverMethod = deliverMethod;
        this.delivered = new HashSet<>();
        this.askForPackets = null;
        this.maxSequenceNumberDelivered = new HashMap<>();
        this.msgAcked = 0;

        // Starting the socket
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is responsible for the parsing of the received payload.
     * It will rebuild the messages and add them to the delivered list
     * @param msg
     */
    public void deliver(MessagePacket msg){
        synchronized (this.maxSequenceNumberDelivered){
            // if I already delivered a bigger message for that original sender, I don't need it
            // it will be the FIFO asking for this restriction, not the URB
            if(this.maxSequenceNumberDelivered.getOrDefault(msg.getOriginalSenderID(), -1) > msg.getPacketID()){
                return;
            }
        }
        Triplet triplet = new Triplet(msg.getPacketID(), msg.getSenderID(), msg.getOriginalSenderID());
        synchronized (this.delivered){
            if(!delivered.contains(triplet)){
                delivered.add(triplet);
                this.deliverMethod.apply(msg);
            }
        }

    }

    /**
     * This method is responsible for removing packets for which an ack has
     * been received from the re_send queue.
     *
     * @param ack is the packet containing the ack
     */
    public void receiveAck(AckPacket ack){
        synchronized (this.maxSequenceNumberDelivered){
            if(this.maxSequenceNumberDelivered.getOrDefault(ack.getOriginalSenderID(), -1) > ack.getPacketID()){
                if(ack.getPacketID() < this.maxSequenceNumberDelivered.get(ack.getOriginalSenderID())){
                    return;
                }
            }
        }
        synchronized (acked){
            acked.add(ack);
        }

        synchronized (this.msgAcked){
            this.msgAcked++;
        }
    }

    /**
     * This function will add the packet to the send queue so that it can be
     * sent (eventually)
     * @param packet is the packet to be sent
     */
    public void sendPacket(MessagePacket packet){
        toSend.add(new AbstractMap.SimpleEntry<>(packet, 1));
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

            try {
//                System.out.println("Queue length: " + toSend.size());
//                queueSize = toSend.size();
//                synchronized (this.msgAcked){
//                    if(this.msgAcked > 8) {
//                        this.msgAcked = 0;
//                        this.askForPackets.apply(null);
//                    }
//                }
//                if(queueSize < 50){
////                    System.out.println("ASKING FOR MSGS");
//
//                    if(!this.askForPackets.apply(null)){
//                        Thread.yield();
//                    }
//                }
                //System.out.println(toSend.size());
                if(toSend.size() < 300){
                    this.askForPackets.apply(null);
                }
                Map.Entry<MessagePacket, Integer> toSendPair = toSend.take();
                MessagePacket msgPkt = toSendPair.getKey();
                // TODO optimize
//                if(toSendPair.getValue() > 1024){
//                    if(!this.askForPackets.apply(null)){
//                        Thread.yield();
//                    }
//                }
                synchronized (acked){
                    AckPacket ack = new AckPacket(msgPkt.getSenderID(),
                            msgPkt.getOriginalSenderID(), msgPkt.getPacketID(), msgPkt.getPort());

                    if(acked.contains(ack)){
                        continue;
                    }
                }

                byte[] msgPayload = msgPkt.serializePacket();
                DatagramPacket datagramPacket =
                        new DatagramPacket(msgPayload, msgPayload.length,
                                msgPkt.getIpAddress(), msgPkt.getPort());
                ds.send(datagramPacket);
                toSendPair.setValue(toSendPair.getValue() * 2);
                toSend.add(toSendPair);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void stopThread(){
        synchronized (this.stop){
            this.stop = true;
        }
    }

    /**
     * Remove the packets up until that sequence number and stop allowing them
     */
    public void removeHistory(byte process, int newMaxSequenceNumber){

        // remove from acked
        synchronized (this.acked){
            acked.removeIf(ack -> ack.getOriginalSenderID() == process && ack.getPacketID() < newMaxSequenceNumber);
        }

        // remove from delivered
        synchronized (this.delivered){
            this.delivered.removeIf(t -> t.getOriginalSenderID() == process && t.getPacketID() < newMaxSequenceNumber);
        }

        // update the newMax for the given process
        synchronized (this.maxSequenceNumberDelivered){
            this.maxSequenceNumberDelivered.put(process, newMaxSequenceNumber);
        }

        //System.out.println("PL history cleaned: acks -> " + acked.size() + " delivered -> " + delivered.size());

    }

    public void setAskForPackets(Function<Void, Boolean> askForPackets) {
        this.askForPackets = askForPackets;
        new Thread(this::send).start();
    }
}
