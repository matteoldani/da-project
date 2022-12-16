package cs451.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProposalParser {

    private BufferedReader file;
    private int numberOfProposal;
    private int maxElementInProposal;
    private int maxDistinctElement;
    private String path;

    public boolean populate(String value){
        try {
            this.file = new BufferedReader(new FileReader(value));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String line = null;
        try {
            line = this.file.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] numbers = line.strip().split(" ");

        this.numberOfProposal       = Integer.parseInt(numbers[0]);
        this.maxElementInProposal   = Integer.parseInt(numbers[1]);
        this.maxDistinctElement     = Integer.parseInt(numbers[2]);

        return true;
    }

    public List<Set<Integer>> getProposals(){
        List<Set<Integer>> proposals = new ArrayList<>();
        for(int i=0; i<200; i++){
            String line = null;
            try {
                line = this.file.readLine();
                if(line == null){
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String[] listNumbers = line.strip().split(" ");
            Set<Integer> newList = new HashSet<>();
            for(int j=0; j<listNumbers.length; j++){
                newList.add(Integer.parseInt(listNumbers[j]));
            }
            proposals.add(newList);
        }
        return proposals;
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
}
