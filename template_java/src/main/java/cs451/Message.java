package cs451;

public class Message {

    private int ID;
    private byte[] payload;

    public Message(int ID, byte[] payload){
        this.ID = ID;
        this.payload = payload;
    }

    public int getID() {
        return ID;
    }

    public byte[] getPayload() {
        return payload;
    }


}
