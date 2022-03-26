package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.*;

public class TestModelEvent implements Event<Model> {

    Model model;

    public TestModelEvent(Model m){
        model = m;
    }

    @Override
    public Model get() {
        return model;
    }
}
