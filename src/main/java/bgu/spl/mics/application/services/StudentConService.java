package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConfrenceBroadcast;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.objects.Student;


public class StudentConService extends MicroService {
    private Student student;
    Thread studentServ;

    public StudentConService(String name, Student s) {
        super(name);
        student = s;
    }
    public void SetOtherServiceThread(Thread studentConThread){
        studentServ = studentConThread;

    }

    @Override
    protected void initialize() {
        getCountDownLatch().countDown();
        subscribeBroadcast(TerminateBroadcast.class,(t)->{
            if(studentServ != null) {
                studentServ.interrupt();
            }
            terminate();
        });

        subscribeBroadcast(PublishConfrenceBroadcast.class, (pcb) ->{
            if(pcb.getHash().containsKey(student))
                student.addToPublications(pcb.getHash().get(student).get());
            else
                student.addToPaperRead(pcb.getNumOfModels());
        });
    }
}
