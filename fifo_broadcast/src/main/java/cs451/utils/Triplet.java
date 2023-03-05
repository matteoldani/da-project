package cs451.utils;

import java.util.Objects;

public class Triplet {

    private int packetID;
    private byte senderID;
    private byte originalSenderID;

    public Triplet(int packetID, byte senderID, byte originalSenderID){
        this.senderID = senderID;
        this.originalSenderID = originalSenderID;
        this.packetID = packetID;
    }

    public int getPacketID() {
        return packetID;
    }

    public byte getSenderID() {
        return senderID;
    }

    public byte getOriginalSenderID() {
        return originalSenderID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triplet triplet = (Triplet) o;
        return packetID == triplet.packetID && senderID == triplet.senderID && originalSenderID == triplet.originalSenderID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packetID, senderID, originalSenderID);
    }
}
