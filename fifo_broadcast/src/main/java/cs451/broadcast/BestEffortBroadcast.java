package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.link.PerfectLink;
import cs451.packet.MessagePacket;
import cs451.utils.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Function;

public class BestEffortBroadcast extends Broadcast {

    private int pktID;
    private Boolean stopThread;


    public BestEffortBroadcast(List<Host> hosts, byte hostID, Function<MessagePacket, Void> deliverMethod){
        super(hosts, hostID);
        this.pl = new PerfectLink(hosts, hostID, this::deliver);
        this.pktID = 0;
        this.stopThread = false;
        this.deliverMethod = deliverMethod;
    }
    @Override
    public void broadcast(int mStart, int mEnd){
        for(Host h: this.hosts){
            // TODO do not send to myself, just pretend that I delivered it
            try {
                MessagePacket pkt = new MessagePacket(this.hostID, this.hostID, this.pktID, InetAddress.getByName(h.getIp()), h.getPort());
                Message msg;

                // I should be sure that the number of messages fits the packet
                for(int i=mStart; i<mEnd; i++){
                    msg = new Message(i, Utils.fromIntToBytes(i));
                    pkt.addMessage(msg);
                }
                if(this.hostID == h.getId()){
                    pkt.serializePacket();
                    this.deliverMethod.apply(pkt);
                }else{
                    this.pl.sendPacket(pkt);
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        this.pktID++;
    }

    public void broadcast(MessagePacket msg){
        for(Host h: this.hosts){
            // TODO do not send to myself, just pretend that I delivered it
            // TODO be sure that the original sender/real sender are correct
            try {

                MessagePacket pkt = new MessagePacket(InetAddress.getByName(h.getIp()),
                        h.getPort(), msg.getPayloadByte());

                if(h.getId() == msg.getSenderID()){
                    // not sending but this needs to be delivered as well
                    this.deliverMethod.apply(pkt);

                }else {
                    this.pl.sendPacket(pkt);
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public Void deliver(MessagePacket msg){
        this.deliverMethod.apply(msg);
        return null;
    }

    @Override
    public void stopThread() {
        synchronized (this.stopThread){
            this.stopThread = true;
            this.pl.stopThread();
        }
    }

    public PerfectLink getPl(){
        return this.pl;
    }

    @Override
    public void removeHistory(byte process, int newMaxSequenceNumber) {
        // do nothing since I have nothing to clean

        // ask the PL to clean itself
        this.pl.removeHistory(process, newMaxSequenceNumber);
    }

    public int getPktID() {
        return pktID;
    }
}
