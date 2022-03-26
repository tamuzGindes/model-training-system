package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PublishConfrenceBroadcast implements Broadcast {
    int numOfModels;
    ConcurrentHashMap<Student, AtomicInteger> hash;
    public PublishConfrenceBroadcast(ConcurrentHashMap<Student, AtomicInteger> hash, int numOfModels){
        this.hash = hash;
        this.numOfModels = numOfModels;
    }
    public ConcurrentHashMap<Student, AtomicInteger> getHash(){
        return hash;
    }
    public int getNumOfModels(){return numOfModels;}


}
