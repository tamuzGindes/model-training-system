package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

import java.util.concurrent.atomic.AtomicInteger;

public class TickBroadcast implements Broadcast {
    int ticks;
    public TickBroadcast(int ticks){
        this.ticks = ticks;
    }
    public int getTicks(){return ticks;}
}
