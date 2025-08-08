import manager.EpicManager;
import manager.SubtaskManager;
import manager.TaskManager;
import status.TaskStatus;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.HashMap;
import java.util.List;

// Класс для тестов приложения
public class Main {
    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();
        SubtaskManager subtaskManager = new SubtaskManager();
        EpicManager epicManager = new EpicManager();

        Task task_1 = new Task("task_1", "description_1");
        Task task_2 = new Task("task_2", "description_2");
        Epic epic = new Epic("task_1", "description_1");
        Subtask subtask_1 = new Subtask("subtask_1", "description_1");
        Subtask subtask_2 = new Subtask("subtask_2", "description_2");
        taskManager.addTask(task_1);
        taskManager.addTask(task_2);
        epicManager.addTask(epic);
        subtaskManager.addTask(subtask_1, epic);
        subtaskManager.addTask(subtask_2, epic);

        task_1.setStatus(TaskStatus.DONE);
        task_2.setStatus(TaskStatus.IN_PROGRESS);
        subtask_1.setStatus(TaskStatus.DONE);
        subtask_2.setStatus(TaskStatus.NEW);
        taskManager.update(task_1);
        taskManager.update(task_2);
        subtaskManager.update(subtask_1);
        subtaskManager.update(subtask_2);

        List<Epic> epicList = epicManager.getListOfTasks();

        taskManager.removeById(0);
        subtaskManager.removeById(3);
        epicManager.removeById(2);

        List<Task> taskList = taskManager.getListOfTasks();
        List<Subtask> subtasksList = subtaskManager.getListOfTasks();

        List<Epic> epicList_2 = epicManager.getListOfTasks();

    }
}