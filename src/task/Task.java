package task;

import status.TaskStatus;

public class Task {
    private static long counter;
    protected final long id;
    protected String name;
    protected String description;
    protected TaskStatus status;

    public Task(String name, String description){
        id = Task.counter++;
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
    }

    public long getId() {
        return this.id;
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

}
