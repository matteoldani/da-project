package cs451.broadcast;

import cs451.Host;
import cs451.link.PerfectLink;
import cs451.packet.MessagePacket;

import java.util.*;
import java.util.function.Function;

public class UniformReliableBroadcast extends Broadcast{

    private BestEffortBroadcast bestEffortBroadcast;

    /**
     * Data structure containing the message acked:
     *  - Needs store for every message (originalSenderID, packetID) the
     *      list of processes which had acked it (List<SenderID>)
     */

    // TODO intead of the set of byte I can use a counter (at lest now)
//    private HashMap<Map.Entry<Byte, Integer>, Set<Byte>> acks;
    private HashMap<Map.Entry<Byte, Integer>, Byte> acks;

    /**
     * Data structure containing the pending messages:
     *  - Needs to store the pair of sender, message
     */

    private HashSet<Map.Entry<Byte, Integer>> pending;
    private Function<MessagePacket, Void> deliverMethod;

    /**
     * Stores the pair <originalSenderID, pktID>
     *     to be sure to only deliver the real packet once
     */
    private Set<Map.Entry<Byte, Integer>> delivered;

    /**
     * This map stores the last delivred value at FIFO level
     */
    private Map<Byte, Integer> maxSequenceNumberDelivered;


    public UniformReliableBroadcast(List<Host> hosts, byte hostID, Function<MessagePacket, Void> deliverMethod){
        super(hosts, hostID);
        this.acks = new HashMap<>();
        this.pending = new HashSet<>();
        this.bestEffortBroadcast = new BestEffortBroadcast(hosts, hostID, this::deliver);
        this.deliverMethod = deliverMethod;
        this.delivered = new HashSet<>();
        this.maxSequenceNumberDelivered = new HashMap<>();
    }

    @Override
    public Void deliver(MessagePacket msg){

        // check if this is a relevant message to be delivered
        synchronized (this.maxSequenceNumberDelivered) {
            if (this.maxSequenceNumberDelivered.getOrDefault(msg.getOriginalSenderID(), -1) > msg.getPacketID()) {
                return null;
            }
        }

        // check if the message (with the original sender) is inside the acks
        // if yes add the current sender to the list
        // if no, create the entry
        Map.Entry<Byte, Integer> msgKey = new AbstractMap.SimpleEntry<>(msg.getOriginalSenderID(), msg.getPacketID());

        //if this message has already been delivered, do nothing
        synchronized (this.delivered){
            if(delivered.contains(msgKey)){
                return null;
            }
        }

        synchronized (this.acks){
            if(this.acks.containsKey(msgKey)){
                // increase the number of acks
                this.acks.put(msgKey, (byte) (this.acks.get(msgKey) + 1));
            }else{
                // set the first ack
                this.acks.put(msgKey, (byte) 1);
            }
        }

        // check if the message is in pending
        // if not, add it and then trigger the broadcast
        synchronized (this.pending){
            if(!pending.contains(msgKey)){
                pending.add(msgKey);
                // I need to modify the sender of the message itself to the
                // actual sender
                msg.setSenderID(this.hostID);
                // ask to broadcast the new message
                this.bestEffortBroadcast.broadcast(msg);
            }
        }

        synchronized (this.delivered){
            if(canDeliver(msgKey)){
                // If I can deliver, send the message to the level above
                this.delivered.add(msgKey);
                this.deliverMethod.apply(msg);
            }
        }
        return null;
    }

    @Override
    public void broadcast(int mStart, int mEnd) {

        // This is required since I need to append to the pending before
        // actually broadcasting to ensure correctness
        int pktID = bestEffortBroadcast.getPktID();
        // Add the packetID to the pending
        // I may want to trigger the check for the delivery, however I don't
        // think it will exist a situation in which I should deliver now
        synchronized (this.pending) {
            pending.add(new AbstractMap.SimpleEntry<>(this.hostID, pktID));
        }
        bestEffortBroadcast.broadcast(mStart, mEnd);

    }

    @Override
    public void stopThread() {
        this.bestEffortBroadcast.stopThread();
    }

    @Override
    public PerfectLink getPl() {
        return this.bestEffortBroadcast.getPl();
    }

    private boolean canDeliver(Map.Entry<Byte, Integer> msgKey){
        // if I have a pending message which has received a majority of acks, then I can deliver it
        if(pending.contains(msgKey) && acks.getOrDefault(msgKey, (byte) 0) > (this.hosts.size()/2)){
            return true;
        }
        return false;
    }

    @Override
    /**
     * This function is called by the FIFO broadcast after a delivery.
     * URB can clean its data structures to free memory since we have already
     * delivered up until this point with the FIFO. In particular pending,
     * delivered and acks needs to be cleaned
     *
     * @param process is the process ID of the process that we want to clean
     * @param newMaxSequenceNumber is the last delivered message of that process
     */
    public void removeHistory(byte process, int newMaxSequenceNumber){

        // update the hashmap
        //System.out.println("update the hashmap");
        synchronized (this.maxSequenceNumberDelivered) {
            this.maxSequenceNumberDelivered.put(process, newMaxSequenceNumber);
        }

        //System.out.println("Clean the pending");
        synchronized (this.pending) {
            // clean the pending
            pending.removeIf(pair -> pair.getKey() == process && pair.getValue() < newMaxSequenceNumber);
        }

        //System.out.println("Clean the acks");
        synchronized (this.acks) {
            // clean the AKCs
            this.acks.keySet().removeIf(pair -> pair.getKey() == process && pair.getValue() < newMaxSequenceNumber);
        }

        //System.out.println("clean the delivered");
        synchronized (this.delivered) {
            // clean the delivered
            this.delivered.removeIf(pair -> pair.getKey() == process && pair.getValue() < newMaxSequenceNumber);
        }

        //System.out.println("Clean the PL");
        this.bestEffortBroadcast.removeHistory(process, newMaxSequenceNumber);

        //System.out.println("PL history cleaned: acks -> " + acks.size() + " delivered -> " + delivered.size() + " pending -> " + pending.size());

    }

}
