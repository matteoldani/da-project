package cs451.packet;

import cs451.utils.Utils;

import java.util.Objects;

public class AckPacket extends Packet{

    public AckPacket(byte[] payload){
        super();
        this.type = PacketType.ACK;
        this.senderID = payload[MessagePacketConstants.SENDER_ID];
        this.packetID = Utils.fromBytesToInt(payload, MessagePacketConstants.PACKET_ID);
        this.port = Utils.fromBytesToInt(payload, MessagePacketConstants.PACKET_ID + 4);

    }

    public AckPacket(byte senderID, int packetID, int port){
        super();
        this.type = PacketType.ACK;
        this.senderID = senderID;
        this.packetID = packetID;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AckPacket ackPacket = (AckPacket) o;
        return port == ackPacket.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), port);
    }
}
