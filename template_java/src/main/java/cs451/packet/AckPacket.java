package cs451.packet;

import cs451.utils.Utils;

import java.util.Objects;

public class AckPacket extends Packet{

    private int port;
    public AckPacket(byte[] payload){
        super();
        this.type = PacketType.ACK;
        this.senderID = payload[1];
        this.originalSenderID = payload[2];
        this.packetID = Utils.fromBytesToInt(payload, 3);
        this.port = Utils.fromBytesToInt(payload, 7);

    }

    public AckPacket(byte senderID, byte originalSenderID, int packetID, int port){
        super();
        this.type = PacketType.ACK;
        this.senderID = senderID;
        this.originalSenderID = originalSenderID;
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
