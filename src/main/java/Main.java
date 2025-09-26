package main.java;

import main.java.manager.FileBackedTaskManager;
import main.java.manager.TaskManager;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new FileBackedTaskManager();
        manager.addTask(new Task("task1", "description4"));
        manager.addEpic(new Epic("epic", "description4"));
        manager.addSubTask(new SubTask("subtask", "description4", 1));
    }
}