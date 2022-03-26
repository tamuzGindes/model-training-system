package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {


    public enum Status {
        PreTrained, Training, Trained, Tested
    }

    public enum Result {
        None, Good, Bad
    }

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Result results;

    public Model(String name, Data data, Student s, Status status, Result result) {
        this.name = name;
        this.data = data;
        this.student = s;
        this.status = status;
        this.results = result;
    }

    public String getName() {
        return name;
    }

    public Data getData() {
        return data;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student s){
        this.student = s;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Result getResults() {
        return results;
    }

    public void setResults(Result res) {
        results = res;
    }

}
