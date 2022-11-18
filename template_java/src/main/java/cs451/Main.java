package cs451;

import cs451.parser.ConfigParser;
import cs451.parser.Parser;
import cs451.process.Process;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class  Main {

    private static String outFile;
    private static Process process;

    private static void handleSignal() {

        // immediately stop network packet processing
        process.stopThread();


        //write/flush output file if necessary
        System.out.println("Writing output.");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(outFile))) {
            List<Integer> processBroadcasted = process.getBroadcasted();
            for(Integer id: processBroadcasted){
                String message = "b " + id.toString() + '\n';
                bufferedWriter.write(message);
            }

            List<Map.Entry<Integer, Byte>> processDelivered = process.getDelivered();
            for (Map.Entry<Integer, Byte> pair:
                    processDelivered) {
                String message = "d " + pair.getValue().toString() + " " + pair.getKey().toString() + '\n';
                bufferedWriter.write(message);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Output written.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT "
                + pid + "` or `kill -SIGTERM "
                + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");

        outFile = parser.output();

        ConfigParser configParser = new ConfigParser();
        configParser.populate(parser.config());

        process = new Process(parser.hosts(), (byte)parser.myId(), configParser.getnumberOfMsgs());

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
