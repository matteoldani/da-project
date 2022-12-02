package cs451.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ConfigParser {

    private String path;
    private int numberOfMsgs;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String number = lines.get(0).strip();
            int m = Integer.parseInt(number);
            this.numberOfMsgs = m;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public String getPath() {
        return path;
    }

    public int getnumberOfMsgs() {
        return numberOfMsgs;
    }
}
