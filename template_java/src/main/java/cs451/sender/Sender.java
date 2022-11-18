package cs451.sender;

import cs451.Host;
import cs451.broadcast.Broadcast;

import java.util.*;

public class Sender{

    private byte hostID;
    private int m;
    private List<Integer> broadcasted;
    private Boolean stop;
    private Broadcast broadcast;

    private int toSend;

    public Sender(Host host, int m, Broadcast broadcast){
        this.m = m;
        this.hostID = (byte)host.getId();

        this.broadcast = broadcast;
        this.broadcasted = new ArrayList<>();

        this.stop = false;

        this.toSend = 1;

        broadcast.getPl().setAskForPackets(this::askForPackets);
    }

    public void stopThread(){
        synchronized (this.stop){
            this.stop = true;
            this.broadcast.stopThread();
        }
    }

    public List<Integer> getBroadcasted() {
        return broadcasted;
    }

    public Boolean askForPackets(Void v){

        if(toSend>m){
            return false;
        }

        for(int times=0; times<3; times++) {

            // check if I still have 8 packets to send
            if ((toSend) + 8 <= m) {
                for (int j = toSend; j < toSend + 8; j++) {
                    this.broadcasted.add(j);
                }
                broadcast.broadcast(toSend, toSend + 8);
                this.toSend += 8;
            } else {
                for (int i = toSend; i <= m; i++) {
                    this.broadcasted.add(i);
                }
                broadcast.broadcast(toSend, m + 1);
                this.toSend = m+1;
                return true;
            }
        }

        return true;
    }
}
