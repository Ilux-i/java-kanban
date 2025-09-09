package main.java.task;

import main.java.status.TaskStatus;

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

}
