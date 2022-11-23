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
    private Map<Byte, Integer> processLastDelivery;
    private Broadcast broadcast;

    private Boolean stop;

    public Process(List<Host> hosts, byte hostID, int nMessages){

        this.hostID = hostID;
        this.hosts = hosts;
        this.stop = false;
        this.delivered = new LinkedList<>();
        this.processLastDelivery = new HashMap<>();

        broadcast = new FIFOBroadcast(hosts, hostID, this::deliver);

        server = new ReceiverServer(broadcast.getPl(),
                40,
                hosts.get(hostID-1).getPort(),
                hosts);

        new Thread(server).start();
        new Thread(this::removeHistory).start();

        sender = new Sender(hosts.get(hostID-1), nMessages, broadcast, (byte)hosts.size());
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

            synchronized (this.processLastDelivery){
                this.processLastDelivery.put(msg.getOriginalSenderID(), msg.getPacketID());
            }

        }

        return null;
    }

    private void removeHistory(){
        while(true){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }
            Map<Byte, Integer> clonedMap = new HashMap<>();
            synchronized (this.processLastDelivery) {
                for (Byte b : this.processLastDelivery.keySet()) {
                    clonedMap.put(b, this.processLastDelivery.get(b));
                }
            }
            // trigger cleanup
            //System.out.println("Triggering remove history");
            for (Byte process : clonedMap.keySet()) {
                //System.out.println("Asking the uniform reliable to remvoe history");
                this.broadcast.removeHistory(process, clonedMap.get(process));
            }
            //System.out.println("Done with remove history");

            System.gc();
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<Map.Entry<Integer, Byte>> getDelivered(){
            return this.delivered;
    }

    public void stopThread(){
        synchronized (this.stop){
            this.stop = true;
        }
        server.stopThread();
        sender.stopThread();
    }

    public List<Integer> getBroadcasted() {
        return this.sender.getBroadcasted();
    }
}
