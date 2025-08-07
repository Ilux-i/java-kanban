import manager.EpicManager;
import manager.SubtaskManager;
import manager.TaskManager;
import status.TaskStatus;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.HashMap;

// Класс для тестов приложения
public class Main {
    public static void main(String[] args) {
        Task task_1 = new Task("task_1", "description_1");
        Task task_2 = new Task("task_2", "description_2");
        Epic epic = new Epic("task_1", "description_1");
        Subtask subtask_1 = new Subtask("subtask_1", "description_1");
        Subtask subtask_2 = new Subtask("subtask_2", "description_2");
        TaskManager.addTask(task_1);
        TaskManager.addTask(task_2);
        EpicManager.addTask(epic);
        SubtaskManager.addTask(subtask_1, epic);
        SubtaskManager.addTask(subtask_2, epic);

        task_1.setStatus(TaskStatus.DONE);
        task_2.setStatus(TaskStatus.IN_PROGRESS);
        subtask_1.setStatus(TaskStatus.DONE);
        subtask_2.setStatus(TaskStatus.NEW);
        TaskManager.update(task_1);
        TaskManager.update(task_2);
        SubtaskManager.update(subtask_1);
        SubtaskManager.update(subtask_2);

        TaskManager.removeById(0);
        SubtaskManager.removeById(3);
        EpicManager.removeById(2);
    }
}