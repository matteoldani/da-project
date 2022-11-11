package cs451.broadcast;

import cs451.link.Link;
import cs451.link.PerfectLink;
import cs451.packet.MessagePacket;

import java.util.function.Function;

public abstract class Broadcast {

    protected Function<MessagePacket, Void> deliverMethod;
    protected PerfectLink pl;

    public abstract void broadcast(int mStart, int mEnd);
    public abstract void broadcast(MessagePacket msg);
    public abstract Void deliver(MessagePacket msg);
    public abstract void stopThread();
    public abstract PerfectLink getPl();

}
