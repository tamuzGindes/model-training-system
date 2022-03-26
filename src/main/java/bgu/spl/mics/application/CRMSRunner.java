package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

class JSONOrderedObject extends LinkedHashMap<String, Object> implements Map<String, Object>, JSONAware, JSONStreamAware {


    @Override
    public String toJSONString() {

        return JSONObject.toJSONString(this);
    }

    @Override
    public void writeJSONString(Writer writer) throws IOException {

        JSONObject.writeJSONString(this, writer);
    }
}

/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) {
        BlockingDeque<Student> studentsList = new LinkedBlockingDeque<Student>();
        ConcurrentHashMap<StudentService, StudentConService> studentServicesList = new ConcurrentHashMap<StudentService, StudentConService>();

        BlockingDeque<GPU> GPUSList = new LinkedBlockingDeque<GPU>();
        ConcurrentHashMap<GPUService, GPUClusterService> GPUServicesList = new ConcurrentHashMap<GPUService, GPUClusterService>();

        BlockingDeque<CPU> CPUSList = new LinkedBlockingDeque<CPU>();
        BlockingDeque<CPUService> CPUServicesList = new LinkedBlockingDeque<CPUService>();

        BlockingDeque<ConfrenceInformation> confrenceList = new LinkedBlockingDeque<ConfrenceInformation>();
        BlockingDeque<ConferenceService> ConferenceServicesList = new LinkedBlockingDeque<ConferenceService>();

        BlockingDeque<MicroService> servicesList = new LinkedBlockingDeque<MicroService>();
        TimeService timeService;
        LinkedBlockingDeque<Thread> threads = new LinkedBlockingDeque<>();
        AtomicInteger numOfServices = new AtomicInteger(0);
        CountDownLatch countDownLatch;

        long tickTime = 0;
        long duration = 0;

        try {
            Object obj = new JSONParser().parse(new FileReader(args[0]));
            JSONObject jsonObj = (JSONObject) obj;

            //Students
            JSONArray Student = (JSONArray) jsonObj.get("Students");
            Iterator itr = Student.iterator();
            while (itr.hasNext()) {
                Object current = itr.next();
                JSONObject currStudent = (JSONObject) current;
                List<Model> modelArray = new ArrayList<Model>();
                String name = (String) currStudent.get("name");
                String department = (String) currStudent.get("department");
                String status = (String) currStudent.get("status");
                //Models
                Data data;
                JSONArray findModels = (JSONArray) currStudent.get("models");
                Iterator itrM = findModels.iterator();
                while (itrM.hasNext()) {
                    JSONObject modelJson = (JSONObject) itrM.next();
                    String modelName = (String) modelJson.get("name");
                    String modelType = (String) modelJson.get("type");
                    long modelSize = (long) modelJson.get("size");
                    if (modelType.equals(Data.Type.Images.name())) {
                        data = new Data(Data.Type.Images, (int) modelSize);
                    } else if (modelType.equals(Data.Type.Text.name())) {
                        data = new Data(Data.Type.Text, (int) modelSize);
                    } else {
                        data = new Data(Data.Type.Tabular, (int) modelSize);
                    }
                    Model model = new Model(modelName, data, null, Model.Status.PreTrained, Model.Result.None);
                    modelArray.add(model);
                }
                Student student = new Student(name, department, status, modelArray);
                studentsList.add(student);
                StudentService studentS = new StudentService("StudentService", student);
                numOfServices.incrementAndGet();
                StudentConService studConSer = new StudentConService("StudentConService", student);
                numOfServices.incrementAndGet();
                servicesList.add(studentS);
                servicesList.add(studConSer);
                studentServicesList.put(studentS, studConSer);
            }

            JSONArray GPU = (JSONArray) jsonObj.get("GPUS");
            Iterator itr1 = GPU.iterator();
            while (itr1.hasNext()) {
                String type = (String) itr1.next();
                GPU gpu = new GPU(type);
                GPUSList.add(gpu);
                GPUClusterService GPUClusterServ = new GPUClusterService("GPUClusterService", gpu);
                numOfServices.incrementAndGet();
                GPUService GPUServ = new GPUService("GPUservice", gpu);
                servicesList.add(GPUServ);
                servicesList.add(GPUClusterServ);
                numOfServices.incrementAndGet();
                GPUServicesList.put(GPUServ, GPUClusterServ);
            }

            JSONArray CPU = (JSONArray) jsonObj.get("CPUS");
            Iterator itr2 = CPU.iterator();
            while (itr2.hasNext()) {
                long cores = (long) itr2.next();
                CPU cpu = new CPU((int) cores);
                CPUSList.add(cpu);
                Cluster.getInstance().addCPU(cpu);
                CPUService CPUServ = new CPUService("CPUservice", cpu);
                servicesList.add(CPUServ);
                numOfServices.incrementAndGet();
                CPUServicesList.add(CPUServ);
            }

            //Confrence
            JSONArray Confrence = (JSONArray) jsonObj.get("Conferences");
            Iterator itr3 = Confrence.iterator();
            while (itr3.hasNext()) {
                JSONObject jCon = (JSONObject) itr3.next();
                String name = (String) jCon.get("name");
                long date = (long) jCon.get("date");
                ConfrenceInformation conference = new ConfrenceInformation(name, (int) date);
                confrenceList.add(conference);
                ConferenceService conServ = new ConferenceService("ConferenceService", conference);
                servicesList.add(conServ);
                numOfServices.incrementAndGet();
                ConferenceServicesList.add(conServ);
            }
            tickTime = (long) jsonObj.get("TickTime");
            duration = (long) jsonObj.get("Duration");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
//start threads
        countDownLatch = new CountDownLatch(numOfServices.get());
        timeService = new TimeService("TimeService", tickTime, (int) duration);
        Thread timeServiceThread = new Thread(timeService, timeService.getName());

        for (CPUService cpuService : CPUServicesList) {
            Thread threadCpu = new Thread(cpuService);
            threads.add(threadCpu);

        }
        for (Map.Entry entry : GPUServicesList.entrySet()) {
            GPUService g = (GPUService) entry.getKey();
            GPUClusterService c = (GPUClusterService) entry.getValue();
            Thread thread1Gpu = new Thread(c);
            Thread thread2Gpu = new Thread(g);
            threads.add(thread1Gpu);
            threads.add(thread2Gpu);

        }
        for (ConferenceService confService : ConferenceServicesList) {
            Thread threadCon = new Thread(confService);
            threads.add(threadCon);
        }

        for (Map.Entry entry : studentServicesList.entrySet()) {
            StudentService s = (StudentService) entry.getKey();
            StudentConService c = (StudentConService) entry.getValue();
            Thread thread1Student = new Thread(s);
            c.SetOtherServiceThread(thread1Student);
            Thread thread2Student = new Thread(c);
            threads.add(thread1Student);
            threads.add(thread2Student);
        }
        timeService.setCountDownLatch(countDownLatch);
        for (MicroService m : servicesList) {
            m.setCountDownLatch(countDownLatch);
        }
        for (Thread t : threads) {
            t.start();
        }

        timeServiceThread.start();

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            timeServiceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

////*** Start of parsing the output file ***

        FileWriter file = null;
        JSONOrderedObject output = new JSONOrderedObject();

//Students
        JSONArray Students = new JSONArray();
        for (Student student : studentsList) {
            JSONOrderedObject objStudent = new JSONOrderedObject();
            //Models
            JSONArray Models = new JSONArray();
            for (Map.Entry entry : student.getModelList().entrySet()) {
                JSONOrderedObject objModel = new JSONOrderedObject();
                JSONOrderedObject objData = new JSONOrderedObject();
                Model mdl = (Model) entry.getValue();
                objModel.put("name", mdl.getName());
                objData.put("dataType", mdl.getData().getType().name());
                objData.put("size", mdl.getData().getSize());
                objModel.put("data", objData);
                Models.add(objModel);
            }
            objStudent.put("name", student.getName());
            objStudent.put("department", student.getDepartment());
            objStudent.put("status", student.getStatus().name());
            objStudent.put("publications", student.getPublications());
            objStudent.put("papersRead", student.getPapersRead());
            objStudent.put("trainedModels", Models);
            Students.add(objStudent);
        }
        output.put("Students", Students);
        output.put("cpuTimeUsed", (int) Statistics.getInstance().getUnitsCPU());
        output.put("gpuTimeUsed", (int) Statistics.getInstance().getUnitsGPU());
        output.put("batchesProcessed", Statistics.getInstance().getNumOfProcessedDataBatches());

        try {
            file = new FileWriter("Output.txt");
            file.write(output.toJSONString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.flush();
                file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
