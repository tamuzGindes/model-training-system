package bgu.spl.mics.application.objects;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {



    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;
    private int limit;
    private AtomicInteger numOfBatchesSent;
    private BlockingQueue<DataBatch> unProcessedDataBatches; // some data structure
    private BlockingQueue<DataBatch> VRAM; // some data structure for processed batches
    private BlockingQueue<DataBatch> trainedBatches; // some data structure for trained batches
    private DataBatch trainedDataBatch;
    private AtomicBoolean sent;
    private AtomicBoolean trained;
    private AtomicInteger trainTime;
    private AtomicInteger trainTickCount;
    private AtomicBoolean doneTrainingModel;
    private AtomicBoolean doneTestingModel;

    private AtomicBoolean terminate;
    private AtomicBoolean trainMode;
    private AtomicBoolean testMode;


    public GPU(String type) {
        switch (type) {
            case "RTX3090":
                limit = 32;
                trainTime = new AtomicInteger(1);
                this.type = Type.RTX3090;
                break;
            case "RTX2080":
                limit = 16;
                trainTime = new AtomicInteger(2);
                this.type = Type.RTX2080;
                break;
            case "GTX1080":
                limit = 8;
                trainTime = new AtomicInteger(4);
                this.type = Type.GTX1080;
                break;
        }
        this.model = null;
        cluster = Cluster.getInstance();
        unProcessedDataBatches = new LinkedBlockingDeque<DataBatch>();
        VRAM = new ArrayBlockingQueue<DataBatch>(limit);
        trainedBatches = new LinkedBlockingDeque<DataBatch>();
        numOfBatchesSent = new AtomicInteger(0);
        trainTickCount = new AtomicInteger(0);
        sent = new AtomicBoolean(false);
        trained = new AtomicBoolean(false);
        trainMode = new AtomicBoolean(false);
        testMode = new AtomicBoolean(false);
        doneTestingModel = new AtomicBoolean(false);
        terminate =  new AtomicBoolean(false);

    }

    public Model getModel() {
        return model;
    }
    public String getType() {
        return type.name();
    }
    public BlockingQueue<DataBatch> getVRAM(){ return VRAM;}

    public Queue<DataBatch> getTrainedBatches() {
        return trainedBatches;
    }
    public void setTrainMode() {
        trainMode.set(true);
        testMode.set(false);
    }
    public Queue<DataBatch> getUnProcessedDataBatches() {
        return unProcessedDataBatches;
    }
    public void setTestMode() {
        trainMode.set(false);
        trainedBatches.clear();
        testMode.set(true);
    }
    public void setModel(Model model) {
        this.model = model;
    }

    public void makeBatches() {
        if (model != null) {
            Data data = model.getData();
            AtomicInteger numOfLoops = new AtomicInteger(data.getSize() / 1000);
            AtomicInteger start_index = new AtomicInteger(0);
            DataBatch dBatch;
            while (numOfLoops.get() > 0) {
                dBatch = new DataBatch(data, start_index.get());
                start_index.addAndGet(1000);
                numOfLoops.decrementAndGet();
                unProcessedDataBatches.add(dBatch);
            }
        }
    }


    public AtomicBoolean processModel(Model m) {
        this.model = m;
        numOfBatchesSent.set(0);
        makeBatches();
        model.setStatus(Model.Status.Training);
        doneTrainingModel = new AtomicBoolean(false);
        return doneTrainingModel;
    }

    public void addToVRAM(DataBatch dBatch) {
        VRAM.add(dBatch);
    }

    public void getDataToTrain() {
        if (!VRAM.isEmpty()) {
            trainedDataBatch = VRAM.poll();
            numOfBatchesSent.decrementAndGet();
            trainTickCount.set(0);
        } else {
            trainedDataBatch = null;
        }
    }

    public void trainBatch() {
        trainTickCount.incrementAndGet();
        trained.set(true);
        if (trainTickCount.get() == trainTime.get() && !trainedDataBatch.isTrained()) {
            trainedDataBatch.getData().addTrainedData(1000);
            trainedBatches.add(trainedDataBatch);
            trainedDataBatch.setTrained(true);
            getDataToTrain();
        }
        if (model.getData().getSize() == model.getData().getTrained()) {
            model.setStatus(Model.Status.Trained);
            cluster.addModelName(model.getName());
            synchronized (doneTrainingModel) {
                doneTrainingModel.set(true);
                doneTrainingModel.notifyAll();
            }
        }
    }

        public void testModel(Model m, AtomicBoolean completed) {
        Random rand = new Random();
        this.model = m;
        if (model.getStudent().getStatus() == Student.Status.MSc) {
            int randOut = rand.nextInt(100);
            boolean randMsc = randOut < 60;
            if (randMsc){
                model.setResults(Model.Result.Good);
            }
            else {
                model.setResults(Model.Result.Bad);
            }
        } else {
            int randOut = rand.nextInt(100);
            boolean randPhd = randOut < 80;
            if (randPhd){
                model.setResults(Model.Result.Good);
            }
            else {
                model.setResults(Model.Result.Bad);
            }
        }
        synchronized (doneTestingModel) {
            doneTestingModel = completed;
            model.setStatus(Model.Status.Tested);
            doneTestingModel.set(true);
            doneTestingModel.notifyAll();
        }
    }

    private void sendBatch() {
        if (numOfBatchesSent.get() < limit - 1 & !unProcessedDataBatches.isEmpty()) {
            sent.set(true);
            DataBatch dBatch = unProcessedDataBatches.poll();
            cluster.process(dBatch, this);
            numOfBatchesSent.incrementAndGet();
        }
    }
    public void terminate(){
        terminate.set(true);
        model = null;
        if(doneTrainingModel != null) {
            synchronized (doneTrainingModel) {
                doneTrainingModel.set(true);
                doneTrainingModel.notifyAll();
            }
        }
        if(doneTestingModel != null) {
            synchronized (doneTestingModel) {
                doneTestingModel.set(true);
                doneTestingModel.notifyAll();
            }
        }
    }
    public void increaseTicks() {
        if (trainMode.get()) {
            sendBatch();
            if (trainedDataBatch == null)
                getDataToTrain();
            if (trainedDataBatch != null)
                trainBatch();
            if (trained.get() | sent.get()) {
                cluster.getStatistics().advanceUnitsGPU();
            }
        }
        if (testMode.get()) {
            cluster.getStatistics().advanceUnitsGPU();
        }
    }
}