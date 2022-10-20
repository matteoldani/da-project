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
