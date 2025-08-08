package task;

import java.util.ArrayList;

public class Epic extends Task{

    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description){
        super(name, description);
        subtasks = new ArrayList<>();
    }

    public ArrayList<Subtask> getSubtasks() {
        return this.subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }
}
