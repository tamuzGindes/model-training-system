package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */

    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Status {
        MSc, PhD
    }

    private String name;
    private String department;
    private Status status;
    private int publications;
    private int papersRead;
    ConcurrentHashMap<Integer, Model> modelList;


    public Student(String name, String depart, String status, List<Model> modelList) {
        this.name = name;
        this.department = depart;
        if (status.equals("MSc"))
            this.status = Status.MSc;
        else
            this.status = Status.PhD;
        this.publications = 0;
        this.papersRead = 0;
        this.modelList = new ConcurrentHashMap<Integer, Model>();
        for (int i = 0; i < modelList.size(); i++) {
            modelList.get(i).setStudent(this);
            this.modelList.put(i, modelList.get(i));
        }
    }
    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public int getPublications() {
        return publications;
    }

    public void addToPublications(int toAdd) {
        publications += toAdd;
    }

    public void addToPaperRead(int toAdd) {
        papersRead += toAdd;
    }

    public ConcurrentHashMap<Integer, Model> getModelList() {
        return modelList;
    }
}
