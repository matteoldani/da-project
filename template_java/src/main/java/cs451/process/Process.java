package cs451.process;

import cs451.Host;
import cs451.broadcast.Broadcast;
import cs451.broadcast.FIFOBroadcast;
import cs451.packet.MessagePacket;
import cs451.sender.Sender;
import cs451.server.ReceiverServer;
import cs451.utils.Utils;

import java.util.*;

public class Process {

    private Sender sender;
    private ReceiverServer server;
    private List<Map.Entry<Integer, Byte>> delivered;
    private List<Host> hosts;
    private byte hostID;

    public Process(List<Host> hosts, byte hostID, int nMessages){

        this.hostID = hostID;
        this.hosts = hosts;

        this.delivered = new LinkedList<>();

        Broadcast b = new FIFOBroadcast(hosts, hostID, this::deliver);

        server = new ReceiverServer(b.getPl(),
                40,
                hosts.get(hostID-1).getPort(),
                hosts);

        sender = new Sender(hosts.get(hostID-1), nMessages, b);

        new Thread(server).start();
    }

    private Void deliver(MessagePacket msg){

        byte senderID = msg.getOriginalSenderID();
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
            synchronized (delivered) {
                delivered.add(e);
            }
        }

        return null;
    }

    public List<Map.Entry<Integer, Byte>> getDelivered(){
        synchronized (delivered){
            return this.delivered;
        }
    }

    public void stopThread(){
        server.stopThread();
        sender.stopThread();
    }

    public List<Integer> getBroadcasted() {
        return this.sender.getBroadcasted();
    }
}
