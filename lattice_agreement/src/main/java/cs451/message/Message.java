package cs451.message;

public abstract class Message {

    public abstract Byte[] serialize();
    protected abstract void deserialize();

    public abstract Byte[] getPayload();
}
