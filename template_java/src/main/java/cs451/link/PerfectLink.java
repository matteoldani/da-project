package cs451.link;

import cs451.Host;
import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;
import cs451.utils.Pair;
import cs451.utils.SystemParameters;
import cs451.utils.Triplet;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class PerfectLink extends Link{

    private BlockingQueue[] toSend;
    private Set<AckPacket> acked;
    private Set<Pair> delivered;
    private DatagramSocket ds;
    private Boolean stop;
    private Function<MessagePacket, Void> deliverMethod;
    private List<Host> hosts;
    private Map<Integer, Byte> portToHost;

    private AtomicInteger[] differenceSendAck;
    private int[] ackReset;

    private byte hostID;

    public PerfectLink(List<Host> hosts, byte hostID, Function<MessagePacket, Void> deliverMethod){

        // Created the two queues used to handle sending of a message

        this.toSend = new LinkedBlockingQueue[128];
        this.differenceSendAck = new AtomicInteger[128];
        this.ackReset = new int[128];

        for(int i=0; i<128; i++){
            this.toSend[i] = new LinkedBlockingQueue<Map.Entry<MessagePacket, Integer>>();
            differenceSendAck[i] = new AtomicInteger(0);
            ackReset[i] = 0;
        }

        this.acked = new HashSet<>();
        this.stop = false;
        this.deliverMethod = deliverMethod;
        this.delivered = new HashSet<>();

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
        }

        Thread t = new Thread(this::send);
        t.start();
    }

    /**
     * This method is responsible for the parsing of the received payload.
     * It will rebuild the messages and add them to the delivered list
     * @param msg
     */
    public void deliver(MessagePacket msg){
        Pair pair = new Pair(msg.getPacketID(), msg.getSenderID());
        synchronized (this.delivered){
            if(!delivered.contains(pair)){
                delivered.add(pair);
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
        this.differenceSendAck[portToHost.get(ack.getPort())].decrementAndGet();
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
        toSend[portToHost.get(packet.getPort())].add(new Pair<>(packet, 1));
    }

    /**
     * This method is run inside a thread, it will continuously try to get new
     * message from the sending queue. If no messages, the Thread will yield to
     * not slow down the application
     */
    private void send(){

        int sleepTime = (int) (this.hosts.size() * (SystemParameters.MAX_DS * 0.01 + 1) * 0.1);
        sleepTime = Math.max(10, sleepTime);

        System.out.println("Initial sleep time: " + sleepTime);

        while(true){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }

            try {
                for(int i=1; i<=hosts.size(); i++){
                    if(i==hostID){continue;}
                    if(toSend[i].size() == 0){
                        continue;
                    }
                    Pair<MessagePacket, Integer> toSendPair = (Pair<MessagePacket, Integer>) toSend[i].take();
                    MessagePacket msgPkt = toSendPair.getKey();

                    synchronized (acked){
                        AckPacket ack = new AckPacket(msgPkt.getSenderID(), msgPkt.getPacketID(), msgPkt.getPort());
                        if(acked.contains(ack)){
                            acked.remove(ack);
                            continue;
                        }
                    }

//                    System.out.println("Difference between send and ack for process " + portToHost.get(msgPkt.getPort()) + " is " + differenceSendAck[portToHost.get(msgPkt.getPort())].get());
                    if(differenceSendAck[portToHost.get(msgPkt.getPort())].get() > 1028/this.hosts.size()){
                        this.ackReset[i]++;
                        toSend[i].add(toSendPair);

                        if(ackReset[i] == 30){
                            differenceSendAck[portToHost.get(msgPkt.getPort())].set(0);
                            ackReset[i] = 0;
                        }
                        continue;
                    }

                    Byte[] msgPayload = msgPkt.serializePacket();
                    byte[] toSendPayload = new byte[msgPayload.length];

                    for(int j=0; j<msgPayload.length; j++){
                        toSendPayload[j] = msgPayload[j];
                    }
                    DatagramPacket datagramPacket =
                            new DatagramPacket(toSendPayload, msgPayload.length,
                                    msgPkt.getIpAddress(), msgPkt.getPort());
                    ds.send(datagramPacket);
                    differenceSendAck[portToHost.get(msgPkt.getPort())].incrementAndGet();
                    toSend[i].add(toSendPair);
                }

                Thread.sleep(15);
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
        //System.out.println("PL history cleaned: acks -> " + acked.size() + " delivered -> " + delivered.size());
    }

}
