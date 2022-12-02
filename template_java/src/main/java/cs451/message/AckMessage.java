package cs451.message;

import cs451.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AckMessage extends Message{

    private MessageType type;
    private int proposalNumber;
    private int activeProposal;

    private Byte[] payload;

    public AckMessage(int proposalNumber, int activeProposal){
        this.type = MessageType.ACK;
        this.proposalNumber = proposalNumber;
        this.activeProposal = activeProposal;
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
        for(int i=0; i<proposalNumber.length; i++){
            payloadList.add(proposalNumber[i]);
        }

        // add the active proposal
        byte[] activeProposal = Utils.fromIntToBytes(this.activeProposal);
        for(int i=0; i<activeProposal.length; i++){
            payloadList.add(activeProposal[i]);
        }

        this.payload = (Byte[]) payloadList.toArray();
        return this.payload;
    }

    protected void deserialize(){
        this.type = MessageType.ACK;

        int pos = 1;

        // proposal number
        this.proposalNumber = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;
        // get the active proposal
        this.activeProposal = Utils.fromBytesToInt(this.payload, pos);
    }

    public MessageType getType() {
        return type;
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public int getActiveProposal() {
        return activeProposal;
    }

    public Byte[] getPayload() {
        return payload;
    }
}
