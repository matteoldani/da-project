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

    private BlockingQueue<MessagePacket> toSend;
    private Set<AckPacket> acked;
    private Set<Triplet> delivered;
    private DatagramSocket ds;
    private Boolean stop;
    private Function<MessagePacket, Void> deliverMethod;
    private Function<Void, Boolean> askForPackets;
    private Map<Byte, Integer> maxSequenceNumberDelivered;

    public PerfectLink(Function<MessagePacket, Void> deliverMethod){

        // Created the two queues used to handle sending of a message
        this.toSend = new LinkedBlockingQueue<>();
        this.acked = new HashSet<>();
        this.stop = false;
        this.deliverMethod = deliverMethod;
        this.delivered = new HashSet<>();
        this.askForPackets = null;
        this.maxSequenceNumberDelivered = new HashMap<>();

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
            if(this.maxSequenceNumberDelivered.containsKey(msg.getOriginalSenderID())){
                if(msg.getPacketID() < this.maxSequenceNumberDelivered.get(msg.getOriginalSenderID())){
                    return;
                }
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
        // TODO ENABLE AS OPT
        synchronized (this.maxSequenceNumberDelivered){
            if(this.maxSequenceNumberDelivered.containsKey(ack.getOriginalSenderID())){
                if(ack.getPacketID() < this.maxSequenceNumberDelivered.get(ack.getOriginalSenderID())){
                    return;
                }
            }
        }
        synchronized (acked){
            acked.add(ack);
        }
    }

    /**
     * This function will add the packet to the send queue so that it can be
     * sent (eventually)
     * @param packet is the packet to be sent
     */
    public void sendPacket(MessagePacket packet){
        toSend.add(packet);
    }

    /**
     * This method is run inside a thread, it will continuously try to get new
     * message from the sending queue. If no messages, the Thread will yield to
     * not slow down the application
     */
    private void send(){

        int queueSize = toSend.size();
        int counter = Integer.MAX_VALUE;

        while(true){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }

            try {
                System.out.println("Queue length: " + toSend.size());
                queueSize = toSend.size();
                if(queueSize < 8 || counter > queueSize){
                    System.out.println("ASKING FOR MSGS");
                    counter = 0;
                    if(!this.askForPackets.apply(null)){
                        Thread.yield();
                    }
                }

                MessagePacket msgPkt = toSend.take();
                counter++;
                synchronized (acked){
                    AckPacket ack = new AckPacket(msgPkt.getSenderID(),
                            msgPkt.getOriginalSenderID(), msgPkt.getPacketID(), msgPkt.getPort());

                    if(acked.contains(ack)){
                        // I don't have to re_send it again
                        // I can remove it from the Set
                        // TODO check if correctness is enforced even with the remove enabledty
                        // acked.remove(ack);
                        continue;
                    }
                }
                System.out.println("Sending with PL: " + msgPkt.getOriginalSenderID() + " " + msgPkt.getSenderID() + " " + msgPkt.getPacketID());

                byte[] msgPayload = msgPkt.serializePacket();
                DatagramPacket datagramPacket =
                        new DatagramPacket(msgPayload, msgPayload.length,
                                msgPkt.getIpAddress(), msgPkt.getPort());
                ds.send(datagramPacket);
                toSend.add(msgPkt);
                Thread.sleep(1);
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
//        synchronized (this.acked){
//            Iterator<AckPacket> iterator = acked.iterator();
//            while(iterator.hasNext()) {
//                AckPacket ack = iterator.next();
//                if (ack.getOriginalSenderID() == process && ack.getPacketID() < newMaxSequenceNumber) {
//                    iterator.remove();
//                }
//            }
//        }

        synchronized (this.delivered){
            Iterator<Triplet> iterator = this.delivered.iterator();
            while (iterator.hasNext()){
                Triplet t = iterator.next();
                if(t.getOriginalSenderID() == process && t.getPacketID() < newMaxSequenceNumber){
                    iterator.remove();
                }
            }
        }



        synchronized (this.maxSequenceNumberDelivered){
            if(this.maxSequenceNumberDelivered.containsKey(process)){
                this.maxSequenceNumberDelivered.put(process, newMaxSequenceNumber);
            }
        }

    }

    public void setAskForPackets(Function<Void, Boolean> askForPackets) {
        this.askForPackets = askForPackets;
        new Thread(this::send).start();
    }
}
