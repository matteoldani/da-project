package cs451.broadcast;

import cs451.Host;
import cs451.link.PerfectLink;
import cs451.packet.MessagePacket;

import java.util.*;
import java.util.function.Function;

public class FIFOBroadcast extends Broadcast{

    private UniformReliableBroadcast uniformReliableBroadcast;

    /**
     * Contains the next sequence number for every process
     */
    private int[] next;
    private HashMap<Map.Entry<Byte, Integer>, MessagePacket> pending;

    private Function<MessagePacket, Void> deliverMethod;

    private int deliveryDone;

    private Map<Byte, Integer> processLastDelivery;

    public FIFOBroadcast(List<Host> hosts, byte hostID, Function<MessagePacket, Void> deliverMethod) {
        super(hosts, hostID);

        this.pending = new HashMap<>();
        this.uniformReliableBroadcast = new UniformReliableBroadcast(hosts, hostID, this::deliver);

        this.next = new int[hosts.size()];
        Arrays.fill(this.next, 0);

        this.deliverMethod = deliverMethod;
        this.deliveryDone = 0;
        this.processLastDelivery = new HashMap<>();

        new Thread(this::canDeliver).start();
    }

    @Override
    public void broadcast(int mStart, int mEnd) {
        this.uniformReliableBroadcast.broadcast(mStart, mEnd);
    }

    @Override
    public Void deliver(MessagePacket msg) {

        // check if the message (with the original sender) is inside the acks
        // if yes add the current sender to the list
        // if no, create the entry
        Map.Entry<Byte, Integer> msgKey = new AbstractMap.SimpleEntry<>(msg.getOriginalSenderID(), msg.getPacketID());

        // check if the message is in pending
        // if not, add it and then do the check
        synchronized (pending){
            pending.put(msgKey, msg);
//            System.out.println("Added to pending in the FIFO");
        }
        return null;
    }

    @Override
    public void stopThread() {
        this.uniformReliableBroadcast.stopThread();
    }

    @Override
    public PerfectLink getPl() {
        return this.uniformReliableBroadcast.getPl();
    }

    private void canDeliver(){
        while(true){
            synchronized (pending){
                if(pending.size() == 0){
                    Thread.yield();
                    continue;
                }

                Iterator<Map.Entry<Byte, Integer>> iterator = pending.keySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<Byte, Integer> pair = iterator.next();
                    if(next[pair.getKey() - 1] == pair.getValue()){
                        next[pair.getKey() -1]++;
                        MessagePacket toDeliver = pending.get(pair);
                        iterator.remove();
                        this.deliverMethod.apply(toDeliver);

//                        // since I am delivering with FIFO, I can trigger the cleaning of the perfect link
//                        // to avoid performance issues, I might consider doing it only N delivery from the FIFO
//                        // TODO
//                        this.processLastDelivery.put(toDeliver.getOriginalSenderID(), toDeliver.getPacketID());
//                        if(this.deliveryDone < 100){
//                            this.deliveryDone++;
//                        }else{
//                            // trigger cleanup
//                            for(Byte process : this.processLastDelivery.keySet()){
//                                this.uniformReliableBroadcast.getPl()
//                                        .removeHistory(process, this.processLastDelivery.get(process));
//                            }
//
//                            this.deliveryDone = 0;
//                        }
                    }
                }
            }
        }
    }
}
