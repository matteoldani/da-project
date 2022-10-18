package cs451.link;

import cs451.packet.AckPacket;
import cs451.packet.MessagePacket;
import cs451.packet.Packet;

public abstract class Link {

    public void send_packet(MessagePacket packet){}
    public void deliver(MessagePacket msg){}

    public void receive_ack(AckPacket ack) {
    }
}
