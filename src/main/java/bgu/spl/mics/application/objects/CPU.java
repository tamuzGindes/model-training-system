package bgu.spl.mics.application.objects;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU{
    private int cores;
    private ConcurrentLinkedQueue<DataBatch> unProcessedDataBatches; // some data structure
    private Cluster cluster;
    private AtomicInteger processTime;
    private AtomicInteger timeLeft;
    private AtomicInteger processTickCount;
    private DataBatch currentDataBatch;

    public CPU(int cores) {
        this.cores = cores;
        unProcessedDataBatches = new ConcurrentLinkedQueue<DataBatch>();
        cluster = Cluster.getInstance();
        timeLeft = new AtomicInteger(0);
        processTickCount = new AtomicInteger(0);
        processTime = new AtomicInteger(0);
    }

    public Queue<DataBatch> getUnProcessedDataBatches() {
        return unProcessedDataBatches;
    }

    public void addDataBatch(DataBatch dBatch) {
        unProcessedDataBatches.add(dBatch);
        addToTotalTime(getDataProcessTime(dBatch));
    }

    public void getDataToProcess() {
        if (!unProcessedDataBatches.isEmpty()) {
            currentDataBatch = unProcessedDataBatches.poll();
            processTime.set(getDataProcessTime(currentDataBatch));
            processTickCount.set(0);
            process();
        } else {
            currentDataBatch = null;
        }
    }
    public DataBatch getCurrentDataBatch(){
        return currentDataBatch;
    }

    public void process() {
        timeLeft.decrementAndGet();
        processTickCount.incrementAndGet();
        cluster.getStatistics().advanceUnitsCPU();
        if (!currentDataBatch.isProcessed() && processTickCount.get() == processTime.get()) {
            currentDataBatch.setProcessed(true);
            currentDataBatch.getData().setProcessed(1000);
            Statistics.getInstance().incNumOfProcessedDataBatches();
            cluster.train(currentDataBatch);
            getDataToProcess();
        }
    }

    private int getDataProcessTime(DataBatch db) {
        switch (db.getData().getType()) {
            case Images:
                return (32 / cores) * 4;
            case Text:
                return (32 / cores) * 2;
            case Tabular:
                return (32 / cores) * 1;
        }
        return 0;
    }

    public int getTimeLeft() {
        return timeLeft.get();
    }

    public int addToTotalTime(int toAdd) {
        return timeLeft.addAndGet(toAdd);
    }

    public int getCores() {
        return cores;
    }

    public void increaseTicks() {
        if (currentDataBatch == null) {
            getDataToProcess();
        } else {
            process();
        }
    }

}
