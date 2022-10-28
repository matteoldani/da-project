package cs451.sender;

import cs451.Host;
import cs451.Message;
import cs451.link.StubbornLink;
import cs451.packet.MessagePacket;
import cs451.utils.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Sender implements Runnable{

    private Host host;
    private byte host_id;
    private int remote_port;
    private InetAddress remote_ip;
    private int m;
    private int pkt_id;
    private StubbornLink link;

    private List<Integer> broadcasted;
    private Boolean stop;

    public Sender(Host host, int m, Host dest_host, StubbornLink link){
        this.host = host;
        this.m = m;

        this.pkt_id = 0;
        this.host_id = (byte)host.getId();

        try {
            this.remote_ip =  InetAddress.getByName(dest_host.getIp());
            this.remote_port = dest_host.getPort();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }


        this.link = link;
        this.broadcasted = new ArrayList<>();

        this.stop = false;
    }

    @Override
    public void run() {

        Message msg;
        MessagePacket pkt = new MessagePacket(this.host_id, this.pkt_id, this.remote_ip, this.remote_port);
        this.pkt_id++;

        for(int i=1; i <= m; i++){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }
            msg = new Message(i, Utils.fromIntToBytes(i));
            this.broadcasted.add(i);

            if(!pkt.addMessage(msg)){
                this.link.send_packet(pkt);
                pkt = new MessagePacket(this.host_id, this.pkt_id, this.remote_ip, this.remote_port);
                this.pkt_id++;
                pkt.addMessage(msg);
            }
        }

        if(pkt.getMsgs() > 0){
            this.link.send_packet(pkt);
        }
    }

    public void stop_thread(){
        synchronized (this.stop){
            this.stop = true;
            this.link.stop_thread();
        }

    }

    public StubbornLink getLink() {
        return link;
    }

    public List<Integer> getBroadcasted() {
        return broadcasted;
    }
}
