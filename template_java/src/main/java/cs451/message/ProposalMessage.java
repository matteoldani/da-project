package cs451.message;

import cs451.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProposalMessage extends Message{

    private MessageType type;
    private int proposalNumber;
    private List<Integer> proposedValues;
    private int activeProposal;
    private Byte[] payload;

    public ProposalMessage(int proposalNumber, List<Integer> proposedValues, int activeProposal){
        this.type = MessageType.MSG;
        this.proposalNumber = proposalNumber;
        this.proposedValues = proposedValues;
        this.activeProposal = activeProposal;
        this.payload = null;
    }

    public ProposalMessage(int proposalNumber, int activeProposal){
        this.type = MessageType.MSG;
        this.proposalNumber = proposalNumber;
        this.proposedValues = new ArrayList<>();
        this.activeProposal = activeProposal;
        this.payload = null;
    }

    public ProposalMessage(Byte[] payload){
        this.payload = payload;
        this.proposedValues = new ArrayList<>();
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

        // add the number of integer in the proposed values
        byte[] proposedValueLength = Utils.fromIntToBytes(this.proposedValues.size());
        for(int i=0; i<proposedValueLength.length; i++){
            payloadList.add(proposedValueLength[i]);
        }

        // add each proposed value
        for(int i=0; i<proposedValues.size(); i++){
            byte[] num = Utils.fromIntToBytes(this.proposedValues.get(i));
            payloadList.add(num[0]);
            payloadList.add(num[1]);
            payloadList.add(num[2]);
            payloadList.add(num[3]);
        }

        // add the active proposal
        byte[] activeProposal = Utils.fromIntToBytes(this.activeProposal);
        for(int i=0; i<activeProposal.length; i++){
            payloadList.add(activeProposal[i]);
        }

        this.payload = (Byte[]) payloadList.toArray();
        return this.payload;
    }

    @Override
    protected void deserialize() {

        this.type = MessageType.MSG;

        int pos = 1;

        // proposal number
        this.proposalNumber = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;

        // get the number to read
        int numberOfValues = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;

        for(int i=0; i<numberOfValues; i++){
            this.proposedValues.add(Utils.fromBytesToInt(this.payload, pos));
            pos+=4;
        }

        // get the active proposal
        this.activeProposal = Utils.fromBytesToInt(this.payload, pos);
    }

    public void addProposalValue(int value){
        this.proposedValues.add(value);
    }

    public MessageType getType() {
        return type;
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public List<Integer> getProposedValues() {
        return proposedValues;
    }

    public int getActiveProposal() {
        return activeProposal;
    }

    public void setActiveProposal(int activeProposal) {
        this.activeProposal = activeProposal;
    }

    public Byte[] getPayload() {
        if(this.payload == null){
            return this.serialize();
        }
        return payload;
    }

    public void setPayload(Byte[] payload) {
        this.payload = payload;
    }
}
