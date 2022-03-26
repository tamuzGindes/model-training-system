package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;

    public CPUService(String name, CPU cpu) {
        super(name);
        this.cpu = cpu;
    }
    public int getCPUcores(){
        return cpu.getCores();
    }

    @Override
    protected void initialize() {
        getCountDownLatch().countDown();
        subscribeBroadcast(TerminateBroadcast.class,(t)->{terminate();
        });
        subscribeBroadcast(TickBroadcast.class, (tick) ->{
            cpu.increaseTicks();
        });
    }
}
