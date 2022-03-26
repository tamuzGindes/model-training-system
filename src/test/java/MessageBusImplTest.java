import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.PublishConfrenceBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.services.StudentService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MessageBusImplTest {
    MessageBusImpl msgb;
    PublishConfrenceBroadcast pConBroad;
    StudentService sService;

    @Before
    public void setUp() throws Exception {
        msgb = MessageBusImpl.getInstance();

        ConcurrentHashMap<Student, AtomicInteger> hashMap = new ConcurrentHashMap<Student, AtomicInteger>();
        Model model1 = new Model("TESTm1", new Data(Data.Type.Images, 5000), null, Model.Status.PreTrained, Model.Result.None);
        Model model2 = new Model("TESTm2", new Data(Data.Type.Images, 15000), null, Model.Status.PreTrained, Model.Result.None);
        Model model3 = new Model("TESTm3", new Data(Data.Type.Images, 25000), null, Model.Status.PreTrained, Model.Result.None);
        Model model4 = new Model("TESTm4", new Data(Data.Type.Images, 10000), null, Model.Status.PreTrained, Model.Result.None);
        Model model5 = new Model("TESTm5", new Data(Data.Type.Images, 5000), null, Model.Status.PreTrained, Model.Result.None);

        List<Model> modelList1 = new ArrayList<Model>();
        List<Model> modelList2 = new ArrayList<Model>();
        List<Model> modelList3 = new ArrayList<Model>();

        modelList1.add(model1);
        modelList1.add(model2);
        modelList2.add(model3);
        modelList2.add(model4);
        modelList3.add(model5);

        Student student1 = new Student("TESTs1", "department", "MSc", modelList1);
        Student student2 = new Student("TESTs2", "department", "PhD", modelList2);
        Student student3 = new Student("TESTs3", "department", "MSc", modelList3);

        hashMap.put(student1, new AtomicInteger(student1.getModelList().size()));
        hashMap.put(student2, new AtomicInteger(student1.getModelList().size()));
        hashMap.put(student3, new AtomicInteger(student1.getModelList().size()));
        pConBroad = new PublishConfrenceBroadcast(hashMap, 5);
        sService = new StudentService("test", student1);
    }

    @Test
    public void testEvent() {
        Student student = new Student("Gal", "department", "MSc", new ArrayList<Model>());

        StudentService sService = new StudentService("test", student);
        Model model = new Model("TEST", new Data(Data.Type.Images, 5000), student, Model.Status.PreTrained, Model.Result.None);
        Message test = null;
        TrainModelEvent event = new TrainModelEvent(model);
        msgb.register(sService);
        msgb.subscribeEvent(TrainModelEvent.class, sService);
        msgb.sendEvent(event);
        try {
            test =  msgb.awaitMessage(sService);
        } catch (Exception e) {}
        assertNotNull(test);
        assertEquals(event,test);
        msgb.unregister(sService);
    }
    @Test
    public void testBroadcast() {
        Student student = new Student("Gal", "department", "MSc", new ArrayList<Model>());
        Model model = new Model("TEST", new Data(Data.Type.Images, 5000), student, Model.Status.PreTrained, Model.Result.None);
        StudentService sService = new StudentService("test", student);

        Message test = null;
        TickBroadcast b = new TickBroadcast(1);
        msgb.register(sService);
        msgb.subscribeBroadcast(TickBroadcast.class, sService);
        msgb.sendBroadcast(b);
        try {
            test =  msgb.awaitMessage(sService);
        } catch (Exception e) {}
        assertNotNull(test);
        assertEquals(b,test);
        msgb.unregister(sService);
    }
}