package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class SendModel implements Broadcast {
    private int index;
    public SendModel(int index){
        this.index = index;
    }
    public int getIndex(){
        return index;
    }
}
