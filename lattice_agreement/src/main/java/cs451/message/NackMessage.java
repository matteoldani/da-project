package cs451.message;

import cs451.utils.Utils;
import jdk.jshell.execution.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NackMessage extends Message{

    private MessageType type;
    private int proposalNumber;
    private int activeProposalNumber;
    private Set<Integer> acceptedValues;

    private Byte[] payload;

    public NackMessage(int proposalNumber, int activeProposalNumber, Set<Integer> acceptedValues){
        this.type = MessageType.NACK;
        this.acceptedValues = acceptedValues;
        this.proposalNumber = proposalNumber;
        this.activeProposalNumber = activeProposalNumber;
    }

    public NackMessage(Byte[] payload){
        this.payload = payload;
        this.acceptedValues = new HashSet<>();
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

        // add the number of integer in the proposed values
        byte[] proposedValueLength = Utils.fromIntToBytes(this.acceptedValues.size());
        for (byte b : proposedValueLength) {
            payloadList.add(b);
        }

        // add each proposed value
        for(Integer i: acceptedValues){
            byte[] num = Utils.fromIntToBytes(i);
            payloadList.add(num[0]);
            payloadList.add(num[1]);
            payloadList.add(num[2]);
            payloadList.add(num[3]);
        }

        this.payload = payloadList.toArray(new Byte[0]);
        return this.payload;
    }

    protected void deserialize(){
        this.type = MessageType.NACK;

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
            this.acceptedValues.add(Utils.fromBytesToInt(this.payload, pos));
            pos+=4;
        }
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public int getActiveProposalNumber() {
        return activeProposalNumber;
    }

    public Set<Integer> getAcceptedValues() {
        return acceptedValues;
    }

    public Byte[] getPayload() {
        if(payload == null){
            return this.serialize();
        }
        return payload;
    }
}
