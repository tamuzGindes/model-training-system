package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {



    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int size;
    private int processed;
    private int trained;

    public Data(Type type, int size) {
        this.type = type;
        this.size = size;
        this.processed = 0;
        this.trained = 0;
    }

    public Type getType() {
        return type;
    }

    public int getTrained() {
        return trained;
    }

    public int getProcessed() {
        return processed;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public void addTrainedData(int toAdd) {
        if (toAdd + trained <= size)
            trained += toAdd;
        else
            System.out.println("the size of trained data is bigger then the data size");
    }

    public int getSize() {
        return size;
    }


}