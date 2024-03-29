package cs451;

import cs451.parser.ConfigParser;
import cs451.parser.Parser;
import cs451.parser.ProposalParser;
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

    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void outputWrite(){
        //write/flush output file if necessary
        System.out.println("Writing output.");
        int proposalToWrite = 0;
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(outFile))) {

            while(true){
                Map<Integer, Set<Integer>> processDecided = process.getDecided();
                boolean shouldContinue = false;
                do{
                    synchronized (processDecided){
                        StringBuilder stringBuilder = new StringBuilder();
                        if(processDecided.containsKey(proposalToWrite)){
                            shouldContinue = true;
                            for(Integer n: processDecided.get(proposalToWrite)){
                                stringBuilder.append(n);
                                stringBuilder.append(' ');
                            }
                            stringBuilder.append('\n');
                            bufferedWriter.write(stringBuilder.toString());
                            bufferedWriter.flush();

                            processDecided.remove(proposalToWrite);
                            proposalToWrite++;

                        }
                    }
                }while (shouldContinue);
                Thread.sleep(500);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OutOfMemoryError e){
            return;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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
        ProposalParser configParser = new ProposalParser();
        configParser.populate(args[Constants.CONFIG_VALUE]);
        SystemParameters.MAX_DS = configParser.getMaxDistinctElement();

        if(SystemParameters.MAX_DS > 500 && parser.hosts().size() > 80){
            SystemParameters.REFILL_THRESHOLD = 1;
            SystemParameters.MAX_MESSAGES_IN_PACKET = 1;
            System.out.println("The parameters of the system are now strict");
        }
        process = new Process(parser.hosts(), (byte)parser.myId(), configParser.getNumberOfProposal(),
                configParser.getMaxElementInProposal(), configParser.getMaxDistinctElement(), configParser);

        Thread.sleep(3000);
        new Thread(Main::outputWrite).start();

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
