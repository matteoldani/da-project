package cs451.utils;

public abstract class Utils {


    public static int fromBytesToInt(byte[] payload, int start){
        int value1 = 0;
        int value2 = 0;
        int value3 = 0;
        int value4 = 0;

        int shift = 0;


        value1 += ((int)(payload[start + 3] & 0xFF)) << shift;
        value2 += ((int)(payload[start + 2] & 0xFF)) << (shift + 8);
        value3 += ((int)(payload[start + 1] & 0xFF)) << (shift + 16);
        value4 += ((int)(payload[start] & 0xFF)) << (shift + 24);


        return value1+value2+value3+value4;
    }

    public static byte[] fromIntToBytes(int value){

        byte[] payload = new byte[4];

        payload[3] = (byte) (value & 0xFF);
        payload[2] = (byte) ((value >>> 8) & 0xFF);
        payload[1] = (byte) ((value >>> 16) & 0xFF);
        payload[0] = (byte) ((value >>> 24) & 0xFF);

        return payload;
    }

    public static int fromBytesToInt(Byte[] payload, int start) {
        int value1 = 0;
        int value2 = 0;
        int value3 = 0;
        int value4 = 0;

        int shift = 0;


        value1 += ((int)(payload[start + 3] & 0xFF)) << shift;
        value2 += ((int)(payload[start + 2] & 0xFF)) << (shift + 8);
        value3 += ((int)(payload[start + 1] & 0xFF)) << (shift + 16);
        value4 += ((int)(payload[start] & 0xFF)) << (shift + 24);


        return value1+value2+value3+value4;
    }
}
