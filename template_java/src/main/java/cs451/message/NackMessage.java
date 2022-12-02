package cs451.message;

import cs451.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class NackMessage extends Message{

    private MessageType type;
    private int proposalNumber;
    private List<Integer> acceptedValues;

    private Byte[] payload;

    public NackMessage(int proposalNumber, List<Integer> acceptedValues){
        this.type = MessageType.NACK;
        this.acceptedValues = acceptedValues;
        this.proposalNumber = proposalNumber;
    }

    public NackMessage(int proposalNumber){
        this.type = MessageType.NACK;
        this.proposalNumber = proposalNumber;
        this.acceptedValues = new ArrayList<>();
    }

    public NackMessage(Byte[] payload){
        this.payload = payload;
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
        byte[] proposedValueLength = Utils.fromIntToBytes(this.acceptedValues.size());
        for(int i=0; i<proposedValueLength.length; i++){
            payloadList.add(proposedValueLength[i]);
        }

        // add each proposed value
        for(int i=0; i<acceptedValues.size(); i++){
            byte[] num = Utils.fromIntToBytes(this.acceptedValues.get(i));
            payloadList.add(num[0]);
            payloadList.add(num[1]);
            payloadList.add(num[2]);
            payloadList.add(num[3]);
        }

        this.payload = (Byte[]) payloadList.toArray();
        return this.payload;
    }

    protected void deserialize(){
        this.type = MessageType.NACK;

        int pos = 1;

        // proposal number
        this.proposalNumber = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;

        // get the number to read
        int numberOfValues = Utils.fromBytesToInt(this.payload, pos);
        pos+=4;

        for(int i=0; i<numberOfValues; i++){
            this.acceptedValues.add(Utils.fromBytesToInt(this.payload, pos));
            pos+=4;
        }
    }


}
