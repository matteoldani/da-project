package cs451.packet;

import java.net.InetAddress;
import java.util.Objects;

public abstract class Packet {

    protected PacketType type;
    protected byte senderID;
    protected byte originalSenderID;
    protected int packetID;

    protected InetAddress ipAddress;
    protected int port;
    
    public Packet(byte senderID, byte originalSenderID,
                  int packetID, InetAddress ipAddress,
                  int port, PacketType type){
        this.type = type;
        this.senderID = senderID;
        this.originalSenderID = originalSenderID;
        this.ipAddress = ipAddress;
        this.port = port;
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

    public byte getOriginalSenderID() {
        return originalSenderID;
    }

    public void setSenderID(byte senderID){
        this.senderID = senderID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return senderID == packet.senderID && originalSenderID == packet.originalSenderID && packetID == packet.packetID && type == packet.type && port == packet.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, senderID, originalSenderID, packetID, port);
    }
}
