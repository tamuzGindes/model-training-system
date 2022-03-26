package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;

import java.util.concurrent.atomic.AtomicBoolean;

public class GPUClusterService extends MicroService {
    private GPU gpu;

    public GPUClusterService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
    }

    protected void initialize() {
        getCountDownLatch().countDown();
            subscribeBroadcast(TerminateBroadcast.class,(t)->{terminate();});
            subscribeEvent(TrainModelEvent.class, (trainME) -> {
                    gpu.setTrainMode();
                    AtomicBoolean completedTrain = gpu.processModel(trainME.get());
                    synchronized (completedTrain) {
                        while (!completedTrain.get()) {
                            try {
                                completedTrain.wait();
                            } catch (InterruptedException e) {
                                terminate();
                            }
                        }
                        if(trainME.get() == null){
                            terminate();
                        }
                        complete(trainME, trainME.get());
                    }
        });
        subscribeEvent(TestModelEvent.class, (testME) -> {
            gpu.setTestMode();
            AtomicBoolean completedTest = new AtomicBoolean(false);
            synchronized (completedTest) {
                gpu.testModel(testME.get(), completedTest);
                while (!completedTest.get()) {
                    try {
                        completedTest.wait();
                    } catch (InterruptedException e) {
                        terminate();
                    }
                }
                complete(testME, testME.get());
            }
        });
        }
}
