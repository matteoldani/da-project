package cs451.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ConfigParser {

    private String path;
    private int number_of_msgs;
    private byte receiver_ID;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String[] numbers = lines.get(0).split(" ");
            int m = Integer.valueOf(numbers[0]);
            byte id = Integer.valueOf(numbers[1]).byteValue();
            this.number_of_msgs = m;
            this.receiver_ID = id;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public String getPath() {
        return path;
    }

    public int getNumber_of_msgs() {
        return number_of_msgs;
    }

    public byte getReceiver_ID() {
        return receiver_ID;
    }
}
