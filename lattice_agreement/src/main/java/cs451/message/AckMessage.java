package cs451.message;

import cs451.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AckMessage extends Message{

    private MessageType type;
    private int proposalNumber;

    private int activeProposalNumber;
    private Byte[] payload;

    public AckMessage(int proposalNumber, int activeProposalNumber){
        this.type = MessageType.ACK;
        this.proposalNumber = proposalNumber;
        this.activeProposalNumber = activeProposalNumber;
    }

    public AckMessage(Byte[] paylaod){
        this.payload = paylaod;
        deserialize();
    }

    public Byte[] serialize(){
        List<Byte> payloadList = new ArrayList<>();

        // add the type
        payloadList.add((byte)this.type.ordinal());

        // add the proposal number
        byte[] proposalNumber = Utils.fromIntToBytes(this.proposalNumber);
        for (byte b : proposalNumber) {
            payloadList.add(b);
        }

        byte[] activeProposalNumber = Utils.fromIntToBytes(this.activeProposalNumber);
        for (byte b : activeProposalNumber) {
            payloadList.add(b);
        }

        this.payload = payloadList.toArray(new Byte[0]);
        return this.payload;
    }

    protected void deserialize(){
        this.type = MessageType.ACK;

        int pos = 1;

        // proposal number
        this.proposalNumber = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;
        this.activeProposalNumber = Utils.fromBytesToInt(this.payload, pos);
    }

    public int getProposalNumber() {
        return proposalNumber;
    }
    public int getActiveProposalNumber(){return activeProposalNumber;}

    public Byte[] getPayload() {

        if(payload == null){
            return this.serialize();
        }
        return payload;
    }
}
