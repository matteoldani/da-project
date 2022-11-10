package cs451.broadcast;

import cs451.link.Link;
import cs451.link.PerfectLink;
import cs451.packet.MessagePacket;

import java.util.function.Function;

public abstract class Broadcast {

    protected Function<MessagePacket, Void> deliverMethod;
    protected PerfectLink pl;

    public void broadcast(int mStart, int mEnd){}
    public Void deliver(MessagePacket msg){
        return null;
    }
    public void stopThread(){}
    public PerfectLink getPl(){
        return null;
    }

}
