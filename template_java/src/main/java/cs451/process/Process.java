package cs451.process;

import cs451.Host;
import cs451.broadcast.BestEffortBroadcast;
import cs451.broadcast.Broadcast;
import cs451.broadcast.UniformReliableBroadcast;
import cs451.packet.MessagePacket;
import cs451.sender.Sender;
import cs451.server.ReceiverServer;
import cs451.utils.Utils;

import java.util.*;

public class Process {

    private Sender sender;
    private ReceiverServer server;
    private Set<Map.Entry<Integer, Byte>> delivered;
    private List<Host> hosts;
    private byte hostID;
    private boolean stop;

    public Process(List<Host> hosts, byte hostID, int nMessages){

        this.hostID = hostID;
        this.hosts = hosts;
        this.stop = false;

        this.delivered = new HashSet<>();

        Broadcast b = new UniformReliableBroadcast(hosts, hostID, this::deliver);

        server = new ReceiverServer(b.getPl(),
                40,
                hosts.get(hostID-1).getPort(),
                hosts);

        sender = new Sender(hosts.get(hostID-1), nMessages, b);

        new Thread(server).start();
        new Thread(sender).start();
    }

    private Void deliver(MessagePacket msg){
        byte senderID = msg.getSenderID();
        byte[] payload = msg.getPayloadByte();

        int msgs = msg.getMsgs();

        int pos = 8;
        int messageLen = 4;
        for(int i=0; i<msgs; i++) {

            int idMessage = Utils.fromBytesToInt(payload, pos);
            pos+= messageLen;
            Map.Entry<Integer, Byte> e =
                    new AbstractMap.SimpleEntry<>(idMessage,
                            senderID);
            if (!delivered.contains(e)) {
                delivered.add(e);
            }
        }

        return null;
    }

    public Set<Map.Entry<Integer, Byte>> getDelivered(){
        return this.delivered;
    }

    public void stopThread(){
        server.stopThread();
        sender.stopThread();

    }

    public List<Integer> getBroadcasted() {
        return this.sender.getBroadcasted();
    }
}
