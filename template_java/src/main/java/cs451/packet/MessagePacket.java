package cs451.packet;

import cs451.message.ProposalMessage;
import cs451.utils.Utils;
import static cs451.packet.MessagePacketConstants.*;

import java.net.InetAddress;
import java.util.ArrayList;

//import static cs451.packet.MessagePacketConstants.SENDER_ID;

public class MessagePacket extends Packet {

    private byte msgs;
    private ArrayList<Byte> payload;
    private int remainingSpace;
    private byte [] payloadByte;

    /**
     * constructor to be used when creating a packet to be sent
     * @param senderID the id of the sender of the packet
     */
    public MessagePacket(byte senderID, int packetID,
                         InetAddress remoteIp, int remotePort){
        super(senderID, packetID, remoteIp,
                remotePort, PacketType.MSG);
        this.payload = new ArrayList<>();

        // MAX number of messages allowed in one packet
        this.msgs = 0;
        this.remainingSpace = (1<<16);
        this.payloadByte = null;
    }

    public MessagePacket(InetAddress remoteIp, int remotePort, byte[] payloadByte){
        super(payloadByte[SENDER_ID], Utils.fromBytesToInt(payloadByte, PACKET_ID),
                remoteIp, remotePort, PacketType.MSG);

        this.payloadByte = payloadByte;
        this.msgs = payloadByte[MSGS];
    }

    /**
     * constructor to be used when receiving a packet
     * @param payload
     */
    public MessagePacket(byte[] payload){
        super();

        // I need to parse the message to deliver it (?)
        this.type = PacketType.MSG;
        this.senderID = payload[SENDER_ID];
        this.packetID = Utils.fromBytesToInt(payload, PACKET_ID);

        this.msgs = payload[MSGS];

        this.payloadByte = payload;
    }

    /**
     * adding a message to the payload of the packet to be sent
     * @param msg is the message to be added
     * @return true if the message can be added, false if there is no space
     */
    public boolean addMessage(ProposalMessage msg){

        if(this.msgs == 8){
            return false;
        }

        Byte[] payload = msg.getPayload();

        if(payload.length + MSGS + 1 > this.remainingSpace){
            return false;
        }

        //adding the payload
        for(int i=0; i<msg.getPayload().length; i++){
            this.payload.add(payload[i]);
        }

        this.remainingSpace -= payload.length;
        this.msgs += 1;

        return true;
    }

    /**
     * serializing the packet to an array of bytes to be sent over UDP
     *
     * ProposalMessage structure:
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

        if(this.payloadByte != null){
            return this.payloadByte;
        }

        byte[] payload = new byte[this.payload.size()+8];

        // adding the type to the payload
        payload[TYPE] = (byte) this.type.ordinal();
        // adding sender ID to the payload
        payload[SENDER_ID] = this.senderID;
        // adding packet_ID to payload
        byte[] packetIDTobyte = Utils.fromIntToBytes(packetID);
        for(int i=0; i<packetIDTobyte.length;i++){
            payload[PACKET_ID+i] = packetIDTobyte[i];
        }
        payload[MSGS] = this.msgs;

        for(int i=0; i<this.payload.size(); i++){
            payload[i+MSGS+1] = this.payload.get(i);
        }
        this.payloadByte = payload;
        return payload;
    }

    public int getMsgs() {
        return msgs;
    }

    public byte[] getPayloadByte() {
        return this.payloadByte;
    }

    @Override
    public void setSenderID(byte senderID) {
        // I need to change both the param and the payload byte if it;s defined
        this.senderID = senderID;
        if(this.payloadByte != null){
            this.payloadByte[SENDER_ID] = senderID;
        }
    }
}
