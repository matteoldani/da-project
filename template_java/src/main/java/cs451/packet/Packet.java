package cs451.packet;

import java.net.InetAddress;

public abstract class Packet {

    protected PacketType type;
    protected byte[] bytePayload;
    protected byte senderID;
    protected int packetID;

    protected InetAddress ipAddress;
    protected int port;
    
    public Packet(byte senderID, int packetID, InetAddress ipAddress, int port, PacketType type){
        this.type = type;
        this.senderID = senderID;
        this.ipAddress = ipAddress;
        this.port = port;
        this.packetID = packetID;
    }

    public Packet(byte senderID, int packetID, PacketType type){
        this.type = type;
        this.senderID = senderID;
        this.packetID = packetID;
    }

    public Packet() {

    }

    public byte[] serializePacket(){
        return null;
    }

    public byte getSenderID() {
        return senderID;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public int getPacketID() {
        return packetID;
    }
}
