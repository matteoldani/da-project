package cs451.packet;

import java.net.InetAddress;

public abstract class Packet {

    protected PacketType type;
    protected byte[] byte_payload;
    protected byte sender_ID;
    protected int packet_ID;

    protected InetAddress ip_address;
    protected int port;
    
    public Packet(byte sender_ID, int packet_ID, InetAddress ip_address, int port, PacketType type){
        this.type = type;
        this.sender_ID = sender_ID;
        this.ip_address = ip_address;
        this.port = port;
        this.packet_ID = packet_ID;
    }

    public Packet(byte sender_ID, int packet_ID, PacketType type){
        this.type = type;
        this.sender_ID = sender_ID;
        this.packet_ID = packet_ID;
    }

    public Packet() {

    }

    public byte[] serializePacket(){
        return null;
    }

    protected int fromBytesToInt(byte[] payload){
        int value = 0;
        int shift = 0;

        for(int i=0; i<payload.length; i++){
            value += ((int)(payload[i])) << shift;
            shift += 8;
        }

        return value;
    }

    protected byte[] fromIntToBytes(int value){

        byte[] payload = new byte[4];

        payload[0] = (byte) (value & 0xFF);
        payload[1] = (byte) ((value >>> 8) & 0xFF);
        payload[2] = (byte) ((value >>> 16) & 0xFF);
        payload[3] = (byte) ((value >>> 24) & 0xFF);

        return payload;
    }

    public byte getSender_ID() {
        return sender_ID;
    }

    public byte[] getByte_payload() {
        return byte_payload;
    }

    public PacketType getType() {
        return type;
    }

    public InetAddress getIp_address() {
        return ip_address;
    }

    public void setIp_address(InetAddress ip_address) {
        this.ip_address = ip_address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPacket_ID() {
        return packet_ID;
    }
}
