package main.java;

import main.java.manager.FileBackedTaskManager;

public class Main {
    public static void main(String[] args) {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile("data_test.csv");
//        manager.addTask(new Task("task1", "description4", Duration.ofMinutes(90L), LocalDateTime.now()));
//        manager.addEpic(new Epic("epic", "description4", Duration.ofMinutes(30), LocalDateTime.now()));
//        manager.addSubTask(new SubTask("subtask", "description4", 0, Duration.ofMinutes(30), LocalDateTime.now()));
//        System.out.println(manager.getEpicById(0).getStartTime());
//        System.out.println(manager.getEpicById(0).getDuration().toMinutes());
//        System.out.println(manager.getEpicById(0).getEndTime());
//        System.out.println(manager.getPrioritizedTasks());


    }
}