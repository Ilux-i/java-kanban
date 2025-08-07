package task;

import java.util.HashMap;

public class Epic extends Task{

    private HashMap<Long, Subtask> subtasks;

    public Epic(String name, String description){
        super(name, description);
        subtasks = new HashMap<Long, Subtask>();
    }

    public HashMap<Long, Subtask> getSubtasks() {
        return this.subtasks;
    }
}
