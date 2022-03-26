package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class DataBatch {
    private Data data;
    private int start_index;
    private AtomicBoolean trained;
    private AtomicBoolean processed;

    public DataBatch(Data data, int start_index){
        this.data = data;
        this.start_index = start_index;
        trained = new AtomicBoolean(false);
        processed = new AtomicBoolean(false);
    }

    public Data getData() {
        return data;
    }

    public boolean isTrained() {
        return trained.get();
    }

    public void setTrained(boolean trained) {
        this.trained.set(trained);
    }
    public boolean isProcessed() {
        return processed.get();
    }

    public void setProcessed(boolean processed) {
        this.processed.set(processed);
    }
}
