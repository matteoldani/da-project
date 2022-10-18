package cs451.packet;

import cs451.Message;

import java.net.InetAddress;
import java.util.ArrayList;

public class MessagePacket extends Packet {

    private int remaining_size;
    private int msgs;
    private ArrayList<Byte> payload;

    /**
     * constructor to be used when creating a packet to be sent
     * @param sender_ID the id of the sender of the packet
     */
    public MessagePacket(byte sender_ID, int packet_ID, InetAddress remote_ip, int remote_port){
        super(sender_ID, packet_ID, remote_ip, remote_port, PacketType.MSG);
        this.payload = new ArrayList<>();
        payload.add(sender_ID);
        // MAX UDP Payload size - space for the sender ID
        this.remaining_size = 65506;
        // MAX number of messages allowed in one packet
        this.msgs = 0;
    }

    /**
     * constructor to be used when receiving a packet
     * @param payload
     */
    public MessagePacket(byte[] payload){
        super();
        // MAX UDP Payload size - space for the sender ID
        this.remaining_size = 65506 - payload.length;
        this.msgs = 0;

        // I need to parse the message to deliver it (?)
    }

    /**
     * adding a message to the payload of the packet to be sent
     * @param msg is the message to be added
     * @return true if the message can be added, false if there is no space
     */
    public boolean addMessage(Message msg){

        if(msg.getPayload().length + 8 > remaining_size){
            return false;
        }

        if(this.msgs == 8){
            return false;
        }

        byte[] id_bytes = fromIntToBytes(msg.getID());
        byte[] payload = msg.getPayload();
        byte[] len_msg = fromIntToBytes(payload.length + 8);

        //adding the message length
        for(int i=0; i<len_msg.length; i++){
            this.payload.add(len_msg[i]);
        }

        //adding the ID
        for(int i=0; i<id_bytes.length; i++){
            this.payload.add(id_bytes[i]);
        }

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
     * @return the packet serialized as a byte array
     */
    public byte[] serializePacket(){
        byte[] payload = new byte[this.payload.size()];
        for(int i=0; i< payload.length; i++){
            payload[i] = this.payload.get(i);
        }

        return payload;
    }

    public int getMsgs() {
        return msgs;
    }
}
