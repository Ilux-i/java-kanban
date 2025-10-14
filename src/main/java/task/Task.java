package main.java.task;

import main.java.status.TaskStatus;
import main.java.status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Task {
    protected static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private static long counter;
    protected long id;
    protected String name;
    protected String description;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description) {
        id = Task.counter++;
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
        this.duration = null;
        this.startTime = null;
    }

    public Task(String name, String description, Duration duration, LocalDateTime startTime) {
        id = Task.counter++;
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String[] list) {
        counter++;
        this.id = Long.parseLong(list[0]);
        this.name = list[2];
        this.status = TaskStatus.valueOf(list[3]);
        this.description = list[4];
        if (list[5].equals("null")) {
            this.duration = null;
            this.startTime = null;
        } else {
            this.duration = Duration.parse(list[5]);
            this.startTime = LocalDateTime.parse(list[6], DATA_TIME_FORMATTER);
        }
    }


    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public String toString() {
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(this.id));
        list.add(TaskType.valueOf(this.getClass().getSimpleName().toUpperCase()).toString());
        list.add(this.name);
        list.add(this.status.toString());
        list.add(this.description);

        if (this.duration == null) {
            list.add("null");
            list.add("null");
        } else {
            list.add(this.duration.toString());
            list.add(this.startTime.format(DATA_TIME_FORMATTER));
        }

        return String.join(",", list);
    }

}
