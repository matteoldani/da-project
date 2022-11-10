package cs451.packet;

import cs451.utils.Utils;

public class AckPacket extends Packet{


    public AckPacket(byte[] payload){
        super();
        this.type = PacketType.ACK;
        this.senderID = payload[1];
        this.packetID = Utils.fromBytesToInt(payload, 2);

    }
}
