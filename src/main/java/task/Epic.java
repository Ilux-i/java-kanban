package main.java.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Epic extends Task {

    private ArrayList<SubTask> subtasks;
    private LocalDateTime endTime;

    public Epic(String name, String description, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
        subtasks = new ArrayList<>();
        this.duration = Duration.ZERO;
        this.endTime = LocalDateTime.of(startTime.toLocalDate(), startTime.toLocalTime());
    }

    public Epic(String[] list, ArrayList<SubTask> subtasks) {
        super(list);
        this.startTime = LocalDateTime.now();
        this.subtasks = subtasks;
        checkingTheEpicExecutionTime();
    }

    public ArrayList<SubTask> getSubtasks() {
        return this.subtasks;
    }

    public LocalDateTime getEndTime(){
        return this.endTime;
    }

    public String toString() {
        String str = super.toString();
        List<String> listId = this.getSubtasks().stream()
                .map(subtask -> String.valueOf(subtask.getId()))
                .collect(Collectors.toList());
        str += "," + String.join(",", listId);
        return str;
    }

    public void checkingTheEpicExecutionTime(){
        Optional<LocalDateTime> endTime = subtasks.stream()
                .map(subTask -> subTask.startTime.plus(subTask.duration)).max((endTime1, endTime2) -> {
                    if (endTime1.isBefore(endTime2)) return -1;
                    if (endTime1.isAfter(endTime2)) return 1;
                    return 0;
                });
        Optional<LocalDateTime> startTime = subtasks.stream()
                .map(subTask -> subTask.startTime)
                .min(Comparator.naturalOrder());

        if(endTime.isPresent()) {
            this.duration = Duration.between(startTime.get(), endTime.get());
            this.endTime = endTime.get();
            this.startTime = startTime.get();
        }

    }
}
