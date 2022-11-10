package cs451.sender;

import cs451.Host;
import cs451.broadcast.Broadcast;

import java.util.*;

public class Sender implements Runnable{

    private byte hostID;
    private int m;
    private List<Integer> broadcasted;
    private Boolean stop;
    private Broadcast broadcast;

    public Sender(Host host, int m, Broadcast broadcast){
        this.m = m;
        this.hostID = (byte)host.getId();

        this.broadcast = broadcast;
        this.broadcasted = new ArrayList<>();

        this.stop = false;
    }

    @Override
    public void run() {

        for(int i=1; i <= m - (m%8); i=i+8){
            synchronized (this.stop){
                if(this.stop){
                    return;
                }
            }
            for(int j=i; j<i+8; j++){
//                System.out.println("Broadcasting " + j + " from: " + hostID);
                this.broadcasted.add(j);
            }

            broadcast.broadcast(i, i+8);
        }

        if(m%8 == 0){return;}
        for(int i = m - (m%8) + 1; i<=m; i++){
//            System.out.println("Broadcasting " + i);
            this.broadcasted.add(i);
        }
        broadcast.broadcast(m - (m%8) + 1, m+1);

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
}
