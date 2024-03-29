package cs451.link;

import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;

public abstract class Link {

    public void sendPacket(MessagePacket packet){}
    public void deliver(MessagePacket msg){}

    public void receiveAck(AckPacket ack) {
    }
}
