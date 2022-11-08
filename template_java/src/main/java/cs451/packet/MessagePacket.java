package cs451.packet;

import cs451.Message;
import cs451.utils.Utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class MessagePacket extends Packet {

    private int remaining_size;
    private byte msgs;
    private ArrayList<Byte> payload;

    private byte [] payload_b;

    /**
     * constructor to be used when creating a packet to be sent
     * @param sender_ID the id of the sender of the packet
     */
    public MessagePacket(byte sender_ID, int packet_ID, InetAddress remote_ip,
                         int remote_port){
        super(sender_ID, packet_ID, remote_ip, remote_port, PacketType.MSG);
        this.payload = new ArrayList<>();

        // MAX number of messages allowed in one packet
        this.msgs = 0;
    }

    /**
     * constructor to be used when receiving a packet
     * @param payload
     */
    public MessagePacket(byte[] payload){
        super();

        // I need to parse the message to deliver it (?)
        this.type = PacketType.MSG;
        this.sender_ID = payload[1];
        this.packet_ID = Utils.fromBytesToInt(payload, 2);

        this.msgs = payload[6];

        this.payload_b = payload;
    }

    /**
     * adding a message to the payload of the packet to be sent
     * @param msg is the message to be added
     * @return true if the message can be added, false if there is no space
     */
    public boolean addMessage(Message msg){

        if(this.msgs == 8){
            return false;
        }

        byte[] payload = msg.getPayload();

        //adding the payload
        for(int i=0; i<msg.getPayload().length; i++){
            this.payload.add(payload[i]);
        }

        this.remaining_size -= (payload.length + 8);
        this.msgs += 1;

        return true;
    }

    /**
     * serializing the packet to an array of bytes to be sent over UDP
     *
     * Message structure:
     *  - Type: byte
     *  - Sender ID: byte
     *  - Packet ID: int -> 4 byte
     *  - n_msgs: byte
     *  - For every message:
     *      - len message: int
     *      - ID message: int
     *      - payload_message: byte[]
     *
     * @return the packet serialized as a byte array
     */
    public byte[] serializePacket(){

        byte[] payload = new byte[this.payload.size()+7];

        // adding the type to the payload
        payload[0] = (byte) this.type.ordinal();
        // adding sender ID to the payload
        payload[1] = sender_ID;
        // adding packet_ID to payload
        byte[] packet_ID_tobyte = Utils.fromIntToBytes(packet_ID);
        for(int i=0; i<packet_ID_tobyte.length;i++){
            payload[2+i] = packet_ID_tobyte[i];
        }
        payload[6] = this.msgs;

        for(int i=0; i<this.payload.size(); i++){
            payload[i+7] = this.payload.get(i);
        }

        return payload;
    }

    public int getMsgs() {
        return msgs;
    }

    public byte[] getPayload_b() {
        return payload_b;
    }
}
