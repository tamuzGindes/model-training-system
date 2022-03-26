package bgu.spl.mics.application.objects;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Statistics {
    private static Statistics statistics = null;
    private BlockingDeque<String> modelsNamesList;
    private int numOfProcessedDataBatches;
    private long unitsCPU;
    private long unitsGPU;

    private Statistics(){
        numOfProcessedDataBatches = 0;
        unitsCPU = 0;
        unitsGPU=0;
        modelsNamesList = new LinkedBlockingDeque<>();
    }

    public static Statistics getInstance() {
        if (statistics == null)
            statistics = new Statistics();
        return statistics;
    }

    public long getUnitsCPU() {
        return unitsCPU;
    }

    public void advanceUnitsCPU() {
        this.unitsCPU++;
    }

    public long getUnitsGPU() {
        return unitsGPU;
    }

    public void advanceUnitsGPU() {
        this.unitsGPU++;
    }

    public BlockingDeque<String> getModelsNamesList() {
        return modelsNamesList;
    }

    public String getModelName(Model model){
        return "";
    }

    public int getNumOfProcessedDataBatches() {
        return numOfProcessedDataBatches;
    }

    public void incNumOfProcessedDataBatches() {
        this.numOfProcessedDataBatches++;
    }

    public void addModelName(String s) {
        modelsNamesList.add(s);
    }
}
