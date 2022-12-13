package cs451.broadcast;

import cs451.Host;
import cs451.message.Message;
import cs451.message.ProposalMessage;
import cs451.link.PerfectLink;
import cs451.packet.MessagePacket;
import cs451.utils.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Function;

public class BestEffortBroadcast extends Broadcast {

    private int pktID;
    private Boolean stopThread;


    public BestEffortBroadcast(List<Host> hosts, byte hostID, Function<MessagePacket, Void> deliverMethod){
        super(hosts, hostID);
        this.pl = new PerfectLink(hosts, hostID, this::deliver);
        this.pktID = 0;
        this.stopThread = false;
        this.deliverMethod = deliverMethod;
    }
    @Override
    public void broadcast(List<Message> messages){
        int pktIDCounter =0;
        for(Host h: this.hosts){
            // TODO do not send to myself, just pretend that I delivered it
            try {
                int i=0;
                pktIDCounter =0;
                while(i<messages.size()){
                    MessagePacket pkt = new MessagePacket(this.hostID, this.pktID + pktIDCounter,
                            InetAddress.getByName(h.getIp()), h.getPort());
                    while(i<messages.size() && pkt.addMessage(messages.get(i))){
                        i++;
                    }
                    pktIDCounter++;
                    if(this.hostID == h.getId()){
                        pkt.serializePacket();
                        this.deliverMethod.apply(pkt);
                    }else{
                        this.pl.sendPacket(pkt);
                    }
                }

            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        this.pktID+=(pktIDCounter+1);
    }

    @Override
    public void broadcast(Message message){
        for(Host h: this.hosts){
            try {
                MessagePacket pkt = new MessagePacket(this.hostID, this.pktID, InetAddress.getByName(h.getIp()), h.getPort());
                pkt.addMessage(message);

                if(this.hostID == h.getId()){
                    pkt.serializePacket();
                    this.deliverMethod.apply(pkt);
                }else{
                    this.pl.sendPacket(pkt);
                }

            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        this.pktID++;
    }

    @Override
    public void send(Message message, Host host) {
        try {
            MessagePacket pkt = new MessagePacket(this.hostID, this.pktID, InetAddress.getByName(host.getIp()), host.getPort());
            pkt.addMessage(message);

            if(this.hostID == host.getId()){
                pkt.serializePacket();
                this.deliverMethod.apply(pkt);
            }else{
                this.pl.sendPacket(pkt);
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        this.pktID++;
    }

    @Override
    public Void deliver(MessagePacket msg){
        this.deliverMethod.apply(msg);
        return null;
    }

    @Override
    public void stopThread() {
        synchronized (this.stopThread){
            this.stopThread = true;
            this.pl.stopThread();
        }
    }

    public PerfectLink getPl(){
        return this.pl;
    }

    @Override
    public void removeHistory(byte process, int newMaxSequenceNumber) {
        // do nothing since I have nothing to clean

        // ask the PL to clean itself
        this.pl.removeHistory(process, newMaxSequenceNumber);
    }

    public int getPktID() {
        return pktID;
    }
}
