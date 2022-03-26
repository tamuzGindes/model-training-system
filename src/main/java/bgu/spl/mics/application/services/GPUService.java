package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.GPU;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private GPU gpu;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
    }
    public String getNameOfGpu(){
        return gpu.getType();
    }



    @Override
    protected void initialize() {
        getCountDownLatch().countDown();
        subscribeBroadcast(TerminateBroadcast.class, (t) -> {
            gpu.terminate();
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, (t) -> {
            gpu.increaseTicks();
        });
    }
}
