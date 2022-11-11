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
     *  - Needs store for every message (packetID, senderID) the
     *      list of processes which had acked it (List<SenderID>)
     */

    private HashMap<Map.Entry<Byte, Integer>, Set<Byte>> acks;

    /**
     * Data structure containing the pending messages:
     *  - Needs to store the pair of sender, message
     */

    private HashSet<Map.Entry<Byte, Integer>> pending;

    private List<Host> hosts;
    private int pktID;
    private byte hostID;
    private Function<MessagePacket, Void> deliverMethod;


    public UniformReliableBroadcast(List<Host> hosts, byte hostID, Function<MessagePacket, Void> deliverMethod){
        this.acks = new HashMap<>();
        this.pending = new HashSet<>();
        this.bestEffortBroadcast = new BestEffortBroadcast(hosts, hostID, this::deliver);

        this.hosts = hosts;
        this.hostID = hostID;
        this.deliverMethod = deliverMethod;

    }

    @Override
    public Void deliver(MessagePacket msg){
        // check if the message (with the original sender) is inside the acks
        // if yes add the current sender to the list
        // if no, create the entry
        Map.Entry<Byte, Integer> msgKey = new AbstractMap.SimpleEntry<>(msg.getOriginalSenderID(), msg.getPacketID());
        if(this.acks.containsKey(msgKey)){
            Set<Byte> list = this.acks.get(msgKey);
            // add the sender to the list of processes that have received that message
            list.add(msg.getSenderID());
        }else{
            this.acks.put(msgKey, new HashSet<>());
        }

        // check if the message is in pending
        // if not, add it and then trigger the broadcast
        if(!pending.contains(msgKey)){
            pending.add(msgKey);
            // I need to modify the sender of the message itself to the
            // actual sender
            msg.setSenderID(this.hostID);

            // ask to broadcast the new message
            broadcast(msg);
        }

        // TODO
        // I'm doing the check for the delivey here, It may be worth spawning a separate thread
        if(canDeliver(msgKey)){
            // If I can deliver, send the message to the level above
            this.deliverMethod.apply(msg);
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
        pending.add(new AbstractMap.SimpleEntry<>(this.hostID, pktID));
        bestEffortBroadcast.broadcast(mStart, mEnd);

    }

    @Override
    public void broadcast(MessagePacket msg){
        // TODO just a temporary implementation
        int pktID = bestEffortBroadcast.getPktID();
        pending.add(new AbstractMap.SimpleEntry<>(this.hostID, pktID));
        bestEffortBroadcast.broadcast(msg);
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
        if(pending.contains(msgKey) && acks.containsKey(msgKey) && acks.get(msgKey).size() > (this.hosts.size()/2) + 1){
            return true;
        }
        return false;
    }

}
