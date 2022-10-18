package cs451;

import cs451.link.StubbornLink;
import cs451.parser.ConfigParser;
import cs451.parser.HostsParser;
import cs451.parser.Parser;
import cs451.sender.Sender;
import cs451.server.ReceiverServer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class  Main {

    private static Sender sender;
    private static ReceiverServer receiverServer;

    private static StubbornLink link;
    private static String out_file;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        if(sender != null){
            sender.stop_thread();
        }
        receiverServer.stop_thread();

        //write/flush output file if necessary
        System.out.println("Writing output.");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(out_file))) {
            if(sender != null){
                for(Integer id: sender.getBroadcasted()){
                    String message = "b " + id.toString();
                    bufferedWriter.write(message);
                }
            }

            for (Map.Entry<Integer, Byte> pair:
                    link.getDelivered()) {
                String message = "d " + pair.getValue().toString() + " " + pair.getKey().toString();
                bufferedWriter.write(message);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        // starting the receiver server
        out_file = parser.output();

        ConfigParser configParser = new ConfigParser();
        configParser.populate(parser.config());

        link = new StubbornLink();
        receiverServer = new ReceiverServer(link,
                1024,
                parser.hosts().get(parser.myId()).getPort());
        new Thread(receiverServer).start();

        // I should build a sender only if I'm not the target
        if(configParser.getReceiver_ID() != parser.myId()){
            sender = new Sender(parser.hosts().get(parser.myId()),
                    configParser.getNumber_of_msgs(),
                    parser.hosts().get(configParser.getReceiver_ID()),
                    link);
            new Thread(sender).start();
        }else{
            sender = null;
        }

        System.out.println("Broadcasting and delivering messages...\n");

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
