package cs451.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigParser {

    private String path;
    private int numberOfProposal;
    private int maxElementInProposal;
    private int maxDistinctElement;

    private List<Set<Integer>> proposals;

    public ConfigParser(){
        this.proposals = new ArrayList<>();
    }

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String[] numbers = lines.get(0).strip().split(" ");

            this.numberOfProposal       = Integer.parseInt(numbers[0]);
            this.maxElementInProposal   = Integer.parseInt(numbers[1]);
            this.maxDistinctElement     = Integer.parseInt(numbers[2]);

            for(int i=1; i<lines.size(); i++){
                String line = lines.get(i);
                String[] listNumbers = line.strip().split(" ");
                Set<Integer> newList = new HashSet<>();
                for(int j=0; j<listNumbers.length; j++){
                    newList.add(Integer.parseInt(listNumbers[j]));
                }
                proposals.add(newList);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public String getPath() {
        return path;
    }

    public int getNumberOfProposal() {
        return numberOfProposal;
    }

    public int getMaxElementInProposal() {
        return maxElementInProposal;
    }

    public int getMaxDistinctElement() {
        return maxDistinctElement;
    }

    public List<Set<Integer>> getProposals() {
        return proposals;
    }
}
