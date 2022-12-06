package cs451;

import cs451.parser.ConfigParser;
import cs451.parser.Parser;
import cs451.process.Process;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class  Main {

    private static String outFile;
    private static Process process;

    private static void handleSignal() {

        // immediately stop network packet processing
        if(process == null){return;}
        process.stopThread();

        Set<Integer> toExclude = new HashSet<>();


        //write/flush output file if necessary
        System.out.println("Writing output.");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(outFile))) {

            Map<Integer, Set<Integer>> processDecided = process.getDecided();

            // DEBUG
            System.out.println("Number of proposal decided = " + processDecided.size());
            for(Integer i: processDecided.keySet()){
                System.out.println("Decided proposal: " + i);
            }

            synchronized (processDecided) {
                for(int i=0; i<processDecided.size(); i++){
                    StringBuilder stringBuilder = new StringBuilder();
                    if(!processDecided.containsKey(i)){
                        System.out.println("Missing proposal " + i + " but the total size is: " + processDecided.size());
                        break;
                    }
                    for(Integer n: processDecided.get(i)){
                        stringBuilder.append(n);
                        stringBuilder.append(' ');
                    }
                    stringBuilder.append('\n');
                    bufferedWriter.write(stringBuilder.toString());
                }
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

        outFile = parser.output();
        ConfigParser configParser = new ConfigParser();
        configParser.populate(args[Constants.CONFIG_VALUE]);

        process = new Process(parser.hosts(), (byte)parser.myId(), configParser.getNumberOfProposal(),
                configParser.getMaxElementInProposal(), configParser.getMaxDistinctElement(), configParser.getProposals());

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
