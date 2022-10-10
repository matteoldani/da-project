package cs451;

import java.util.ArrayList;

public class Packet {

    private int remaining_size;
    private int remaining_msgs;
    private ArrayList<Byte> payload;

    /**
     * constructor to be used when creating a packet to be sent
     * @param sender_ID the id of the sender of the packet
     */
    public Packet(byte sender_ID){
        this.payload = new ArrayList<>();
        payload.add(sender_ID);
        this.remaining_size = 65506; // MAX UDP Payload size - space for the sender ID
        this.remaining_msgs = 8;    // MAX number of messages allowed in one packet
    }

    /**
     * constructor to be used when receiving a packet
     * @param payload
     */
    public Packet(byte[] payload){
        this.remaining_size = 65506 - payload.length; // MAX UDP Payload size - space for the sender ID
        this.remaining_msgs = 8;
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

        if(this.remaining_msgs == 0){
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
        this.remaining_msgs -= 1;

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



    private int fromBytesToInt(byte[] payload){
        int value = 0;
        int shift = 0;

        for(int i=0; i<payload.length; i++){
            value += ((int)(payload[i])) << shift;
            shift += 8;
        }

        return value;
    }

    private byte[] fromIntToBytes(int value){

        byte[] payload = new byte[4];

        payload[0] = (byte) (value & 0xFF);
        payload[1] = (byte) ((value >>> 8) & 0xFF);
        payload[2] = (byte) ((value >>> 16) & 0xFF);
        payload[3] = (byte) ((value >>> 24) & 0xFF);

        return payload;
    }


}
