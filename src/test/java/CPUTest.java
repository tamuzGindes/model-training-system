import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.DataBatch;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CPUTest {
    CPU cpu;
    DataBatch dBatch1;
    DataBatch dBatch2;
    @Before
    public void setUp() throws Exception {
        cpu = new CPU(16);
         dBatch1 = new DataBatch(new Data(Data.Type.Images, 1000), 0);
         dBatch2 = new DataBatch(new Data(Data.Type.Text, 1000), 0);

    }

    @Test
    public void addDataBatch() {
        int sizeBefore = cpu.getUnProcessedDataBatches().size();
        assertEquals(0, sizeBefore);
        cpu.addDataBatch(dBatch1);
        int sizeAfter = cpu.getUnProcessedDataBatches().size();
        assertEquals(1, sizeAfter);
        assertEquals(dBatch1, cpu.getUnProcessedDataBatches().poll());
    }

    @Test
    public void process() {
        cpu.addDataBatch(dBatch1);
        cpu.addDataBatch(dBatch2);
        int sizeBefore = cpu.getUnProcessedDataBatches().size();
        assertEquals(2, sizeBefore);
        cpu.increaseTicks();
        assertEquals(0, dBatch1.getData().getProcessed());//because the data batches aren't ready
        cpu.getDataToProcess();
        cpu.increaseTicks();
        assertEquals(0, dBatch2.getData().getProcessed());//because the data batches aren't ready
        int sizeAfter = cpu.getUnProcessedDataBatches().size();
        assertEquals(0, sizeAfter);
    }
}