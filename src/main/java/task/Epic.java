package main.java.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Epic extends Task {

    private ArrayList<SubTask> subtasks;

    public Epic(String name, String description, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
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
