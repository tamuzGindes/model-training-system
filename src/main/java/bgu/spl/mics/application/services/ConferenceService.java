package bgu.spl.mics.application.services;

import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConfrenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    AtomicInteger ticksCount;
    ConfrenceInformation conferenceInfo;
    Message msg;
    int date;
    ConcurrentHashMap<Student, AtomicInteger> modelsStudentMap;
    AtomicInteger numOfModels;

    public ConferenceService(String name,ConfrenceInformation conInfo) {
        super(name);

        conferenceInfo = conInfo;
        ticksCount =  new AtomicInteger(0);
        modelsStudentMap = new ConcurrentHashMap<Student, AtomicInteger>();
        numOfModels = new AtomicInteger(0);
    }
    public String getNameOfCon(){
        return conferenceInfo.getName();
    }


    @Override
    protected void initialize() {
        getCountDownLatch().countDown();
        MessageBusImpl.getInstance().register(this);
        subscribeBroadcast(TerminateBroadcast.class,(t)->{terminate();});

        subscribeBroadcast(TickBroadcast.class,(i)->{
            ticksCount.incrementAndGet();
            if(ticksCount.get() >= conferenceInfo.getDate()) {
                PublishConfrenceBroadcast b = new PublishConfrenceBroadcast(modelsStudentMap,numOfModels.get());
                sendBroadcast(b);
                terminate();
            }
        });

        subscribeEvent(PublishResultsEvent.class ,(e)->{
                if(e.get().getResults() == Model.Result.Good){
                    Student student = e.get().getStudent();
                    if(modelsStudentMap.containsKey(student)){
                        modelsStudentMap.get(student).incrementAndGet();
                        numOfModels.incrementAndGet();
                    }
                    else{
                        modelsStudentMap.put(student,new AtomicInteger(1));
                        numOfModels.incrementAndGet();
                    }
                }
                complete(e,e.get());
        });
    }
}
