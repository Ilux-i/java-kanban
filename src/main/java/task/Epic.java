package main.java.task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Epic extends Task {

    private ArrayList<SubTask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    public Epic(String[] list, ArrayList<SubTask> subtasks) {
        super(list);
        this.subtasks = subtasks;
    }

    public ArrayList<SubTask> getSubtasks() {
        return this.subtasks;
    }

    public String toString() {
        String str = super.toString();
        List<String> listId = this.getSubtasks().stream()
                .map(subtask -> String.valueOf(subtask.getId()))
                .collect(Collectors.toList());
        str += "," + String.join(",", listId);
        return str;
    }
}
