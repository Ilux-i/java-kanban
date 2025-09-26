package main.java.task;

import main.java.status.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Task {
    private static long counter;
    protected long id;
    protected String name;
    protected String description;
    protected TaskStatus status;

    public Task(String name, String description) {
        id = Task.counter++;
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
    }

    public Task(String[] list) {
        counter++;
        this.id = Long.parseLong(list[0]);
        this.name = list[2];
        this.status = TaskStatus.valueOf(list[3]);
        this.description = list[4];

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

    public String toString() {
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(this.id));
        list.add(this.getClass().getSimpleName().toUpperCase());
        list.add(this.name);
        list.add(this.status.toString());
        list.add(this.description);
        return String.join(",", list);
    }

}
