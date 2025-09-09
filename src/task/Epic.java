package task;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<SubTask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    public ArrayList<SubTask> getSubtasks() {
        return this.subtasks;
    }

}
