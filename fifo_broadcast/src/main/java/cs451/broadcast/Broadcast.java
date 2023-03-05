package cs451.broadcast;

import cs451.Host;
import cs451.link.Link;
import cs451.link.PerfectLink;
import cs451.packet.MessagePacket;

import java.util.List;
import java.util.function.Function;

public abstract class Broadcast {

    protected Function<MessagePacket, Void> deliverMethod;
    protected PerfectLink pl;

    protected List<Host> hosts;
    protected byte hostID;

    public Broadcast(List<Host> hosts, byte hostID){
        this.hostID = hostID;
        this.hosts = hosts;
    }

    public abstract void broadcast(int mStart, int mEnd);
    public abstract Void deliver(MessagePacket msg);
    public abstract void stopThread();
    public abstract PerfectLink getPl();
    public abstract void removeHistory(byte process, int newMaxSequenceNumber);

}
