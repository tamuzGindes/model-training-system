package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    private Student student;
    AtomicInteger index = new AtomicInteger(0);

    public StudentService(String name, Student s) {
        super(name);
        student = s;
    }


    @Override
    protected void initialize() {
        getCountDownLatch().countDown();
        Thread x = new Thread(() -> {subscribeBroadcast(TerminateBroadcast.class,(t)->{terminate();});
        subscribeBroadcast(SendModel.class, (send) -> {
            Future<Model> trainFuture = sendEvent(new TrainModelEvent(student.getModelList().get(index.get())));
            if (trainFuture != null) {
                Model resolvedTrain = trainFuture.get();
                if (resolvedTrain != null) {
                    Future<Model> testFuture = sendEvent(new TestModelEvent(resolvedTrain));
                    Model resolvedTest = testFuture.get();
                    if (resolvedTest != null) {
                        if (resolvedTest.getResults() == Model.Result.Good) {
                            Future<Model> publishFuture = sendEvent(new PublishResultsEvent(testFuture.get()));
                        }
                        int nums = index.get() + 1;
                        if (nums < student.getModelList().size()) {
                            index.incrementAndGet();
                            sendBroadcast(new SendModel(index.get()));
                        } else {
                            terminate();
                        }
                    } else {
                        terminate();
                    }
                } else {
                    terminate();
                }
            } else {
                terminate();
            }
        });});
        x.setDaemon(true);
        x.start();
    }

    public Student getStudent() {
        return student;
    }
}
