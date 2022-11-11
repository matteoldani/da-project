package cs451.packet;

import cs451.utils.Utils;

public class AckPacket extends Packet{


    public AckPacket(byte[] payload){
        super();
        this.type = PacketType.ACK;
        this.senderID = payload[1];
        this.originalSenderID = payload[2];
        this.packetID = Utils.fromBytesToInt(payload, 3);

    }

    public AckPacket(byte senderID, byte originalSenderID, int packetID){
        super();
        this.type = PacketType.ACK;
        this.senderID = senderID;
        this.originalSenderID = originalSenderID;
        this.packetID = packetID;
    }
}
