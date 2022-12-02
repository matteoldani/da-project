package cs451.process;

import cs451.Host;
import cs451.broadcast.BestEffortBroadcast;
import cs451.broadcast.Broadcast;
import cs451.packet.MessagePacket;
import cs451.packet.Packet;
import cs451.sender.Sender;
import cs451.server.ReceiverServer;
import cs451.utils.Pair;
import cs451.utils.Utils;
import java.util.*;

public class Process {

    private ReceiverServer server;
    private List<Host> hosts;
    private byte hostID;
    private Broadcast broadcast;

    private int numberOfProposal;
    private int maxNumberInProposal;
    private int maxDistinctNumbers;

    /**
     * this is the list of proposals to make. Every
     * proposal is a list of integers
     */
    private List<List<Integer>> proposals;
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
    /**
     * will store the number of the proposal and the decided values
     * It is in a map so that we can print them in order while deciding
     * out of order (accordingly with network activity)
     */
    private Map<Integer, Set<Integer>> decided;

    private Boolean stop;

    public Process(List<Host> hosts, byte hostID, int numberOfProposal, int maxNumberInProposal,
                   int maxDistinctNumbers, List<List<Integer>> proposals){

        this.hostID = hostID;
        this.hosts = hosts;

        this.numberOfProposal = numberOfProposal;
        this.maxNumberInProposal = maxNumberInProposal;
        this.maxDistinctNumbers = maxDistinctNumbers;
        this.proposals = proposals;

        this.proposalAcks = new HashMap<>();
        this.proposalNacks = new HashMap<>();
        this.activeProposalNumbers = new HashSet<>();
        this.proposedValues = new HashMap<>();
        this.decided = new HashMap<>();

        this.stop = false;

        broadcast = new BestEffortBroadcast(hosts, hostID, this::deliver);

        server = new ReceiverServer(broadcast.getPl(),
                //TODO FIX THIS SIZE
                40,
                hosts.get(hostID-1).getPort(),
                hosts);

        new Thread(server).start();
        new Thread(this::removeHistory).start();

        // TODO I need to create the threads that sends proposal when the active proposal set is
        // TODO below a certain threshold

    }

    private Void deliver(MessagePacket msg){
// TODO
//        byte senderID = msg.getOriginalSenderID();
//        byte[] payload = msg.getPayloadByte();
//        int msgs = msg.getMsgs();
//
//        int pos = 8;
//        int messageLen = 4;
//
//        for(int i=0; i<msgs; i++) {
//            int idMessage = Utils.fromBytesToInt(payload, pos);
//            pos+= messageLen;
//            Map.Entry<Integer, Byte> e =
//                    new AbstractMap.SimpleEntry<>(idMessage,
//                            senderID);
//            synchronized (delivered) {
//                delivered.add(e);
//            }
//
//            synchronized (this.processLastDelivery){
//                this.processLastDelivery.put(msg.getOriginalSenderID(), msg.getPacketID());
//            }
//
//        }

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
