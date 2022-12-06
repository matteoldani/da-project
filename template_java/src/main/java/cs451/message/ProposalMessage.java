package cs451.message;

import cs451.utils.Utils;

import java.util.*;

public class ProposalMessage extends Message{

    private MessageType type;
    private int proposalNumber;
    private int activeProposalNumber;
    private Set<Integer> proposedValues;
    private Byte[] payload;

    public ProposalMessage(int proposalNumber, int activeProposalNumber, Set<Integer> proposedValues){
        this.type = MessageType.MSG;
        this.proposalNumber = proposalNumber;
        this.proposedValues = proposedValues;
        this.activeProposalNumber = activeProposalNumber;
        this.payload = null;
    }

    public ProposalMessage(Byte[] payload){
        this.payload = payload;
        this.proposedValues = new HashSet<>();
        deserialize();
    }

    public Byte[] serialize(){
        List<Byte> payloadList = new ArrayList<>();

        // add the type
        payloadList.add((byte)this.type.ordinal());

        // add the proposal number
        byte[] proposalNumber = Utils.fromIntToBytes(this.proposalNumber);
        for (byte item : proposalNumber) {
            payloadList.add(item);
        }

        // add the active proposal number
        byte[] activeProposalNumber = Utils.fromIntToBytes(this.activeProposalNumber);
        for (byte value : activeProposalNumber) {
            payloadList.add(value);
        }

        // add the number of integer in the proposed values
        byte[] proposedValueLength = Utils.fromIntToBytes(this.proposedValues.size());
        for (byte b : proposedValueLength) {
            payloadList.add(b);
        }

        // add each proposed value
        for(Integer i: proposedValues){
            byte[] num = Utils.fromIntToBytes(i);
            payloadList.add(num[0]);
            payloadList.add(num[1]);
            payloadList.add(num[2]);
            payloadList.add(num[3]);
        }

        this.payload = payloadList.toArray(new Byte[0]);
        return this.payload;
    }

    @Override
    protected void deserialize() {

        this.type = MessageType.MSG;

        int pos = 1;

        // proposal number
        this.proposalNumber = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;

        this.activeProposalNumber = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;

        // get the number to read
        int numberOfValues = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;

        for(int i=0; i<numberOfValues; i++){
            this.proposedValues.add(Utils.fromBytesToInt(this.payload, pos));
            pos+=4;
        }

    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public int getActiveProposalNumber(){ return activeProposalNumber;}

    public Set<Integer> getProposedValues() {
        return proposedValues;
    }

    public Byte[] getPayload() {
        if(this.payload == null){
            return this.serialize();
        }
        return payload;
    }
}
