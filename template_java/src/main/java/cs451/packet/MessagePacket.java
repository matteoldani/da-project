package cs451.packet;

import cs451.Message;
import cs451.utils.Utils;

import java.net.InetAddress;
import java.util.ArrayList;

public class MessagePacket extends Packet {

    private byte msgs;
    private ArrayList<Byte> payload;

    private byte [] payloadByte;

    /**
     * constructor to be used when creating a packet to be sent
     * @param senderID the id of the sender of the packet
     */
    public MessagePacket(byte senderID, int packetID, InetAddress remoteIp,
                         int remotePort){
        super(senderID, packetID, remoteIp, remotePort, PacketType.MSG);
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
        this.senderID = payload[1];
        this.packetID = Utils.fromBytesToInt(payload, 2);

        this.msgs = payload[6];

        this.payloadByte = payload;
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
        payload[1] = senderID;
        // adding packet_ID to payload
        byte[] packetIDTobyte = Utils.fromIntToBytes(packetID);
        for(int i=0; i<packetIDTobyte.length;i++){
            payload[2+i] = packetIDTobyte[i];
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

    public byte[] getPayloadByte() {
        return payloadByte;
    }
}
