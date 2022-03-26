package bgu.spl.mics.application.objects;


import java.util.Comparator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class Cluster {
    private static Cluster cluster = null;
    private static boolean isDone = false;
    private PriorityBlockingQueue<CPU> cpus;
    private Statistics statistics;
    private ConcurrentHashMap<DataBatch, GPU> gpuDataMap;
    BlockingDeque<CPU> cpuList;
    int sizeOfDataToAdd;
    private Cluster() {
        sizeOfDataToAdd = 0;
        statistics = Statistics.getInstance();
        gpuDataMap = new ConcurrentHashMap<DataBatch, GPU>();
        cpuList = new LinkedBlockingDeque<CPU>();
    }
    public void initCluster(){
        cpus = new PriorityBlockingQueue(cpuList.size(), Comparator.comparingInt((CPU cpu) -> ((cpu.getTimeLeft() + sizeOfDataToAdd) / cpu.getCores())));
        while(!cpuList.isEmpty()) {
            CPU c = cpuList.poll();
            cpus.put(c);
        }
    }
    /**
     * Retrieves the single instance of this class.
     */
    public static Cluster getInstance() {
        if (!isDone) {
            synchronized (Cluster.class) {
                if (!isDone) {
                    cluster = new Cluster();
                    isDone = true;
                }
            }
        }
        return cluster;
    }
    public Statistics getStatistics() {
        return statistics;
    }

    public void addModelName(String s) {
        Statistics.getInstance().addModelName(s);
    }

    public void train(DataBatch dBatch) {
        GPU gpu = gpuDataMap.get(dBatch);
        gpu.addToVRAM(dBatch);
    }

    public void process(DataBatch dBatches, GPU gpu) {
        //add dBatches to cpu's queues
        gpuDataMap.put(dBatches, gpu);
        CPU cpu = cpus.poll();
        cpu.addDataBatch(dBatches);
        cpus.put(cpu);
    }

    public void addCPU(CPU cpu) {
        cpuList.add(cpu);
    }
}
