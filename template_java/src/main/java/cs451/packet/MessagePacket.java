package cs451.packet;

import cs451.message.*;
import cs451.utils.Utils;
import static cs451.packet.MessagePacketConstants.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//import static cs451.packet.MessagePacketConstants.SENDER_ID;

public class MessagePacket extends Packet {

    private byte msgs;
    private ArrayList<Byte> payload;
    private int remainingSpace;
    private Byte [] payloadByte;

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

    public MessagePacket(InetAddress remoteIp, int remotePort, Byte[] payloadByte){
        super(payloadByte[SENDER_ID], Utils.fromBytesToInt(payloadByte, PACKET_ID),
                remoteIp, remotePort, PacketType.MSG);

        this.payloadByte = payloadByte;
        this.msgs = payloadByte[MSGS];
    }

    /**
     * constructor to be used when receiving a packet
     * @param payload
     */
    public MessagePacket(Byte[] payload){
        super();

        // I need to parse the message to deliver it (?)
        this.type = PacketType.MSG;
        this.senderID = payload[SENDER_ID];
        this.packetID = Utils.fromBytesToInt(payload, PACKET_ID);

        this.msgs = payload[MSGS];

        this.payloadByte = payload;
    }

    public MessagePacket(byte[] payload){
        super();

        Byte[] correctPayload = new Byte[payload.length];
        for(int i=0; i<payload.length; i++){
            correctPayload[i] = payload[i];
        }

        // I need to parse the message to deliver it (?)
        this.type = PacketType.MSG;
        this.senderID = payload[SENDER_ID];
        this.packetID = Utils.fromBytesToInt(payload, PACKET_ID);

        this.msgs = payload[MSGS];

        this.payloadByte = correctPayload;
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

        Byte[] payload = msg.getPayload();

        if(payload.length + MSGS + 5 > this.remainingSpace){
            return false;
        }

        // adding the length of the payload to the payload
        byte[] payloadLen = Utils.fromIntToBytes(payload.length);
        for(int i=0; i<4; i++){
            this.payload.add(payloadLen[i]);
        }

        //adding the payload
        this.payload.addAll(Arrays.asList(payload));

        this.remainingSpace -= (payload.length + 4); // 4 is the integer used fot the length
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
    public Byte[] serializePacket(){

        if(this.payloadByte != null){
            return this.payloadByte;
        }

        // adding the number of messages to the payload
        this.payload.add(0, this.msgs);
        // adding packet_ID to payload
        byte[] packetIDTobyte = Utils.fromIntToBytes(packetID);
        this.payload.add(0, packetIDTobyte[3]);
        this.payload.add(0, packetIDTobyte[2]);
        this.payload.add(0, packetIDTobyte[1]);
        this.payload.add(0, packetIDTobyte[0]);
        // adding sender ID to the payload
        this.payload.add(0, this.senderID);
        // adding the type to the payload
        this.payload.add(0, (byte) this.type.ordinal());

        Byte[] payload = this.payload.toArray(new Byte[0]);
        this.payloadByte = payload;
        return payload;
    }

    public List<Message> getMessages(){
        if(payloadByte == null){
            return null;
        }
//        System.out.println("Payload byte is: " + Arrays.toString(payloadByte));
        List<Message> listOfMessages = new ArrayList<>();
        int pos = MSGS + 1;
        for(int i=0; i<msgs; i++){
            int messageLen = Utils.fromBytesToInt(payloadByte, pos);
//            System.out.println("Message len is: " + messageLen);
            pos+=4;
            int messageType = payloadByte[pos];
            Message msg;
            switch (messageType){
                case 0: // MSG
                    msg = new ProposalMessage(Arrays.copyOfRange(payloadByte, pos, pos+messageLen));
                    break;
                case 1:
                    msg = new AckMessage(Arrays.copyOfRange(payloadByte, pos, pos+messageLen));
                    break;
                case 2:
                    msg = new NackMessage(Arrays.copyOfRange(payloadByte, pos, pos+messageLen));
                    break;
                default:
                    msg = null;
                    System.err.println("Invalid message type");
                    break;
            }
            pos+=messageLen;
            listOfMessages.add(msg);
        }

        return listOfMessages;
    }

    public int getMsgs() {
        return msgs;
    }

    public Byte[] getPayloadByte() {
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
