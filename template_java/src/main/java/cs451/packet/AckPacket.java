package cs451.packet;

import cs451.utils.Utils;

import java.util.Arrays;

public class AckPacket extends Packet{


    public AckPacket(byte[] payload){
        super();
        this.type = PacketType.ACK;
        this.sender_ID = payload[0];
        this.packet_ID = Utils.fromBytesToInt(Arrays.copyOfRange(payload, 1, 5));

    }
}
