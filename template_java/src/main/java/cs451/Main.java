package cs451;

import cs451.parser.ConfigParser;
import cs451.parser.Parser;
import cs451.process.Process;
import cs451.utils.GlobalMemExceptionHandler;
import cs451.utils.SystemParameters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class  Main {

    private static String outFile;
    private static Process process;

    private static AtomicBoolean isHandled;

    private static void handleSignal() {

        System.out.println("I am in the handle signal");

        if(isHandled.compareAndExchange(false, true)){
            return;
        }

        // immediately stop network packet processing
        if(process == null){return;}
        process.stopThread();

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
                    bufferedWriter.flush();
                }
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OutOfMemoryError e){
            return;
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

        GlobalMemExceptionHandler geh = new GlobalMemExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(geh);

        isHandled = new AtomicBoolean(false);

        initSignalHandlers();

        outFile = parser.output();
        try {
            Runtime.getRuntime().exec("touch " + outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ConfigParser configParser = new ConfigParser();
        configParser.populate(args[Constants.CONFIG_VALUE]);
        SystemParameters.MAX_DS = configParser.getMaxDistinctElement();

        if(SystemParameters.MAX_DS > 500 && parser.hosts().size() > 80){
            SystemParameters.REFILL_THRESHOLD = 2;
            SystemParameters.MAX_MESSAGES_IN_PACKET = 1;
            System.out.println("The parameters of the system are now strict");
        }
        process = new Process(parser.hosts(), (byte)parser.myId(), configParser.getNumberOfProposal(),
                configParser.getMaxElementInProposal(), configParser.getMaxDistinctElement(), configParser.getProposals());

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
