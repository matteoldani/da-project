package cs451.link;

import cs451.Host;
import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;
import cs451.utils.Triplet;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class PerfectLink extends Link{

    private BlockingQueue[] toSend;
    private Set<AckPacket> acked;
    private Set<Triplet> delivered;
    private DatagramSocket ds;
    private Boolean stop;
    private Function<MessagePacket, Void> deliverMethod;
    private Function<Void, Boolean> askForPackets;
    private Map<Byte, Integer> maxSequenceNumberDelivered;
    private List<Host> hosts;
    private Map<Integer, Byte> portToHost;

    private byte hostID;

    public PerfectLink(List<Host> hosts, byte hostID, Function<MessagePacket, Void> deliverMethod){

        // Created the two queues used to handle sending of a message

        this.toSend = new LinkedBlockingQueue[128];
        for(int i=0; i<128; i++){
            this.toSend[i] = new LinkedBlockingQueue<Map.Entry<MessagePacket, Integer>>();
        }

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

        this.hosts = hosts;
        this.hostID = hostID;
        this.portToHost = new HashMap<>();

        for (Host h: hosts) {
            this.portToHost.put(h.getPort(), (byte)h.getId());
            System.out.println(h.getPort() + " " + h.getId());
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
    }

    /**
     * This function will add the packet to the send queue so that it can be
     * sent (eventually)
     * @param packet is the packet to be sent
     */
    public void sendPacket(MessagePacket packet){
        toSend[portToHost.get(packet.getPort())].add(new AbstractMap.SimpleEntry<>(packet, 1));
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
                int totalSizes = 0;
                for(int i=1; i<=hosts.size(); i++){
                    if(i==hostID){continue;}
                    if(toSend[i].size() < ( 1280 * 3)/hosts.size()){
                        this.askForPackets.apply(null);
                    }

                    Map.Entry<MessagePacket, Integer> toSendPair = (Map.Entry<MessagePacket, Integer>) toSend[i].take();
                    MessagePacket msgPkt = toSendPair.getKey();

                    synchronized (acked){
                        AckPacket ack = new AckPacket(msgPkt.getSenderID(),
                                msgPkt.getOriginalSenderID(), msgPkt.getPacketID(), msgPkt.getPort());

                        if(acked.contains(ack)){
                            acked.remove(ack);
                            continue;
                        }
                    }

                    byte[] msgPayload = msgPkt.serializePacket();
                    DatagramPacket datagramPacket =
                            new DatagramPacket(msgPayload, msgPayload.length,
                                    msgPkt.getIpAddress(), msgPkt.getPort());
                    ds.send(datagramPacket);
                    toSend[i].add(toSendPair);
                }
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
