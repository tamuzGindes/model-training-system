package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.SendModel;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Cluster;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {
    long speed;
    long duration;
    AtomicInteger numOfTicks;
    Timer timer;


    public TimeService(String name, long speed, int duration) {
        super(name);
        this.speed = speed;
        this.duration = duration;


    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateBroadcast.class,(t)->{terminate();});
        numOfTicks = new AtomicInteger(1);
        try {
            getCountDownLatch().await();
            Cluster.getInstance().initCluster();
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        try {
            sendBroadcast(new SendModel(0));
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    TickBroadcast tick = new TickBroadcast(numOfTicks.get());
                    sendBroadcast(tick);
                    numOfTicks.incrementAndGet();
                    if (numOfTicks.get() == duration) {
                        sendBroadcast(new TerminateBroadcast());
                        timer.cancel();
                    }
                }
            };
            timer = new Timer();
            timer.schedule(task, 50, speed);
        } catch (Exception e) {
            terminate();
        }

    }
}
