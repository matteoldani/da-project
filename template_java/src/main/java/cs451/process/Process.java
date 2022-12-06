package cs451.process;

import cs451.Host;
import cs451.broadcast.BestEffortBroadcast;
import cs451.broadcast.Broadcast;
import cs451.message.AckMessage;
import cs451.message.Message;
import cs451.message.NackMessage;
import cs451.message.ProposalMessage;
import cs451.packet.MessagePacket;
import cs451.server.ReceiverServer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Process {

    private ReceiverServer server;
    private List<Host> hosts;
    private Broadcast broadcast;

    private Integer numberOfProposal;

    /**
     * this is the list of proposals to make. Every
     * proposal is a list of integers
     */
    private List<Set<Integer>> proposals;
    /**
     * for every proposal I store the number of acks
     */
    private Map<Integer, Integer> proposalAcks;
    /**
     * for every proposal I store the number of nacks
     */
    private Map<Integer, Integer> proposalNacks;

    /**
     * In this set will ersists the active proposeal numeber
     * When the set is below a certain threshold I will start new
     * proposals
     */
    private Set<Integer> activeProposalNumbers;

    /**
     * For every proposal I store the proposed values
     */
    private Map<Integer, Set<Integer>> proposedValues;
    private Map<Integer, Set<Integer>> acceptedValues;
    /**
     * will store the number of the proposal and the decided values
     * It is in a map so that we can print them in order while deciding
     * out of order (accordingly with network activity)
     */
    private Map<Integer, Set<Integer>> decided;

    private Map<Integer, Integer> proposalToActive;

    private Boolean stop;
    private int threshold;

    private byte hostID;

    public Process(List<Host> hosts, byte hostID, int numberOfProposal, int maxNumberInProposal,
                   int maxDistinctNumbers, List<Set<Integer>> proposals){

        this.hosts = hosts;
        this.hostID = hostID;

        this.numberOfProposal = 0;
        this.proposals = proposals;

        this.proposalAcks = new ConcurrentHashMap<>();
        this.proposalNacks = new ConcurrentHashMap<>();
        this.activeProposalNumbers = ConcurrentHashMap.newKeySet();
        this.proposedValues = new ConcurrentHashMap<>();
        this.decided = new ConcurrentHashMap<>();
        this.acceptedValues = new ConcurrentHashMap<>();

        this.stop = false;
        this.threshold = (hosts.size() / 2) + 1;
        this.proposalToActive = new ConcurrentHashMap<>();

        broadcast = new BestEffortBroadcast(hosts, hostID, this::deliver);

        server = new ReceiverServer(broadcast.getPl(),
                //TODO FIX THIS SIZE
                (4*maxNumberInProposal+40)*8,
                hosts.get(hostID-1).getPort(),
                hosts);

        new Thread(server).start();
        new Thread(this::removeHistory).start();

        sendProposal();
        sendProposal();

        // TODO I need to create the threads that sends proposal when the active proposal set is
        // TODO below a certain threshold

    }

    private void sendProposal(){

        // I have to send (8) proposal
        List<Message> proposalMessageList = new ArrayList<>();
        for(int i=0; i<8; i++){
            if(this.proposals.size() == 0){
                break;
            }
            Set<Integer> toPropose = this.proposals.remove(0);

            // take the new proposal number
            int activeProposal = this.numberOfProposal++;
            ProposalMessage proposalMessage = new ProposalMessage(activeProposal, 1, toPropose);
            System.out.println("Propose " + activeProposal + " is: " + Arrays.toString(toPropose.toArray()));

            // add the proposal to the active proposals
            this.activeProposalNumbers.add(activeProposal);
            // specify the active proposal number for this proposal (1 is the starting one)
            this.proposalToActive.put(activeProposal, 1);
            // add the values to be proposed. If the proposed are not null, then simply add to them
            // TODO proably this is an unnecessary computation
            if(this.proposedValues.containsKey(activeProposal)){
                this.proposedValues.get(activeProposal).addAll(toPropose);
            }else{
                this.proposedValues.put(activeProposal, ConcurrentHashMap.newKeySet());
                this.proposedValues.get(activeProposal).addAll(toPropose);
            }
//            this.proposedValues.put(activeProposal, toPropose);
            // also the ack should be inserted
            this.acceptedValues.put(activeProposal, ConcurrentHashMap.newKeySet());
            this.acceptedValues.get(activeProposal).addAll(toPropose);
            this.proposalAcks.put(activeProposal, 1);
            this.proposalNacks.put(activeProposal, 0);
            proposalMessageList.add(proposalMessage);
        }

        this.broadcast.broadcast(proposalMessageList);
    }

    private void ackEvent(int proposalNumber){
        // check if I received enough acks
        if(this.proposalAcks.get(proposalNumber) >= this.threshold){
            System.out.println("Decision " +proposalNumber+" is: " + Arrays.toString(this.proposedValues.get(proposalNumber).toArray()));
            this.decided.put(proposalNumber, this.proposedValues.get(proposalNumber));
            int activeSize;
            synchronized (this.activeProposalNumbers) {
                this.activeProposalNumbers.remove(proposalNumber);
                activeSize = this.activeProposalNumbers.size();
            }

            if(activeSize<8){
                sendProposal();
            }
        }
    }

    private void nackEvent(int proposalNumber){
        if(this.proposalNacks.get(proposalNumber) +
                this.proposalAcks.get(proposalNumber) >= this.threshold){

            int activeProposal = this.proposalToActive.get(proposalNumber) + 1;
            this.proposalToActive.put(proposalNumber, activeProposal);

            this.proposalAcks.put(proposalNumber, 1);
            this.proposalNacks.put(proposalNumber, 0);

            this.acceptedValues.put(proposalNumber, ConcurrentHashMap.newKeySet());
            this.acceptedValues.get(proposalNumber).addAll(this.proposedValues.get(proposalNumber));
            System.out.println("Nack event for proposal " + proposalNumber + " proposes: " + this.proposedValues.get(proposalNumber));
            ProposalMessage pm = new ProposalMessage(proposalNumber, activeProposal,
                    this.proposedValues.get(proposalNumber));

            this.broadcast.broadcast(pm);
        }
    }

    private void handleAcks(AckMessage msg){

        if(!this.activeProposalNumbers.contains(msg.getProposalNumber())){
            return;
        }

        if((this.proposalToActive.get(msg.getProposalNumber()) != msg.getActiveProposalNumber())){
            return;
        }

        // increase the acks for a specific proposal
        this.proposalAcks.put(msg.getProposalNumber(),
                this.proposalAcks.get(msg.getProposalNumber()) + 1);


        ackEvent(msg.getProposalNumber());
        nackEvent(msg.getProposalNumber());
    }

    private void handleNacks(NackMessage msg){
        if(!this.activeProposalNumbers.contains(msg.getProposalNumber())){
            return;
        }
        if((this.proposalToActive.get(msg.getProposalNumber()) != msg.getActiveProposalNumber())){
            System.out.println("Incorrect active proposal value for the Nack: " + this.proposalToActive.get(msg.getProposalNumber()) + " " +msg.getActiveProposalNumber());
            return;
        }

        // TODO check that this actually adds all the values
        this.proposedValues.get(msg.getProposalNumber()).addAll(msg.getAcceptedValues());
        System.out.println("Nack message for proposal " + msg.getProposalNumber() + " proposes: " + this.proposedValues.get(msg.getProposalNumber()));
        this.proposalNacks.put(msg.getProposalNumber(),
                this.proposalNacks.get(msg.getProposalNumber()) + 1);


        // check if I have to broadcast
        nackEvent(msg.getProposalNumber());

    }

    private void handleProposal(ProposalMessage msg, byte senderID){

        if(this.acceptedValues.get(msg.getProposalNumber()) == null){
            this.acceptedValues.put(msg.getProposalNumber(), ConcurrentHashMap.newKeySet());
        }

        if(msg.getProposedValues().containsAll(this.acceptedValues.get(msg.getProposalNumber()))){
            this.acceptedValues.get(msg.getProposalNumber()).addAll(msg.getProposedValues());
            System.out.println("ACK Process " + senderID + " proposal "+ msg.getProposalNumber()+ " is: " + this.acceptedValues.get(msg.getProposalNumber()));
            // I have to send the ack message back
            AckMessage message = new AckMessage(msg.getProposalNumber(), msg.getActiveProposalNumber());
            this.broadcast.send(message, this.hosts.get(senderID-1)); // TODO check if this works
        }else{
//            msg.getProposedValues().addAll(this.acceptedValues.get(msg.getProposalNumber()));
//            this.acceptedValues.put(msg.getProposalNumber(), msg.getProposedValues());
            this.acceptedValues.get(msg.getProposalNumber()).addAll(msg.getProposedValues());
            System.out.println("NACK Process " + senderID + " proposal "+ msg.getProposalNumber()+ " is: " + this.acceptedValues.get(msg.getProposalNumber()));
            NackMessage message = new NackMessage(msg.getProposalNumber(), msg.getActiveProposalNumber(),
                    this.acceptedValues.get(msg.getProposalNumber()));

            this.broadcast.send(message, this.hosts.get(senderID-1));
        }

    }

    /**
     * This function is called by the perfect linkg (actually the breadcast) once a message is received
     * @param msgPkt is the message received
     * @return null
     */
    private Void deliver(MessagePacket msgPkt){

        List<Message> messageList = msgPkt.getMessages();
        for(Message msg: messageList){
            if( msg instanceof ProposalMessage){
                handleProposal((ProposalMessage) msg, msgPkt.getSenderID());
            } else if (msg instanceof AckMessage) {
                handleAcks((AckMessage) msg);
            } else if (msg instanceof  NackMessage) {
                handleNacks((NackMessage) msg);
            } else {
                System.err.println("Message is an incorrect instance");
            }
        }

        return null;
    }

    private void removeHistory(){
        // TODO
    }


    public void stopThread(){
        synchronized (this.stop){
            this.stop = true;
        }
        server.stopThread();
    }

    public Map<Integer, Set<Integer>> getDecided() {
        return decided;
    }

}
