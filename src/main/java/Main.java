package main.java;

import main.java.manager.FileBackedTaskManager;
import main.java.manager.TaskManager;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = FileBackedTaskManager.loadFromFile("data_test.csv");
//        manager.addTask(new Task("task1", "description4", Duration.ofMinutes(90L), LocalDateTime.now()));
//        manager.addEpic(new Epic("epic", "description4", Duration.ofMinutes(30), LocalDateTime.now()));
        manager.addSubTask(new SubTask("subtask", "description4", 0, Duration.ofMinutes(30), LocalDateTime.now()));
        System.out.println(manager.getEpicById(0).getStartTime());
        System.out.println(manager.getEpicById(0).getDuration().toMinutes());
        System.out.println(manager.getEpicById(0).getEndTime());
    }
}