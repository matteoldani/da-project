package cs451.packet;

public class AckPacket extends Packet{


    public AckPacket(byte user_ID, int packet_ID){
        super(user_ID, packet_ID, PacketType.ACK);

    }
}
