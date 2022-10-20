package cs451.utils;

public abstract class Utils {


    public static int fromBytesToInt(byte[] payload, int start){
        int value = 0;
        int shift = 0;

        for(int i=start+3; i>=start; i--){
            value += ((int)(payload[i] & 0xFF)) << shift;
            shift += 8;
        }

        return value;
    }

    public static byte[] fromIntToBytes(int value){

        byte[] payload = new byte[4];

        payload[3] = (byte) (value & 0xFF);
        payload[2] = (byte) ((value >>> 8) & 0xFF);
        payload[1] = (byte) ((value >>> 16) & 0xFF);
        payload[0] = (byte) ((value >>> 24) & 0xFF);

        return payload;
    }

}
