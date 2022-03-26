import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class GPUTest {
    GPU gpu;

    @Before
    public void setUp() throws Exception {
        gpu = new GPU("RTX3090");
        gpu.setModel(new Model("TEST", new Data(Data.Type.Images, 5000), new Student("testSTU", "testDEP", "MSc", new ArrayList<Model>()), Model.Status.PreTrained, Model.Result.None));
    }

    @Test
    public void makeBatches() {
        assertEquals(0, gpu.getUnProcessedDataBatches().size());
        gpu.makeBatches();
        assertEquals(5, gpu.getUnProcessedDataBatches().size());
    }

    @Test
    public void trainBatch() {
        gpu.makeBatches();
        for (int i = 0; i < 5; i++) {
            gpu.addToVRAM(gpu.getUnProcessedDataBatches().poll());
        }
        assertEquals(5, gpu.getVRAM().size());
        assertEquals(0, gpu.getTrainedBatches().size());
        //2 ticks for each batch from this type
        //for (int i = 0; i < 6; i++) {
            gpu.getDataToTrain();
       // }
        assertEquals(4, gpu.getVRAM().size());
        assertEquals(0, gpu.getTrainedBatches().size());
        for (int i = 0; i < 4; i++) {
            gpu.trainBatch();
        }
        assertEquals(4, gpu.getTrainedBatches().size());
        assertEquals(0, gpu.getUnProcessedDataBatches().size());
    }

    @Test
    public void testModel() {
        assertEquals(gpu.getModel().getResults(), Model.Result.None);
        AtomicBoolean test = new AtomicBoolean(false);
        synchronized (test) {
            gpu.testModel(gpu.getModel(), test);
        }
        assertNotEquals(gpu.getModel().getResults(), Model.Result.None);
    }
}