package main.java.manager;

import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.util.List;

public interface TaskManager {
    // Получение всех задач
    List<Task> getListOfTasks();

    List<Epic> getListOfEpics();

    List<SubTask> getListOfSubTasks();

    List<Task> getHistory();

    // Удаление всех задач
    void clearTasks();

    void clearEpics();

    void clearSubTasks();

    // Получение задачи по id
    Task getTaskById(long id);

    Epic getEpicById(long id);

    SubTask getSubTaskById(long id);

    //Создание задачи
    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubTask(SubTask subTask);

    // Обновление задачи
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubTask(SubTask subTask);

    // Удаление задачи по id
    void removeTaskById(long id);

    void removeEpicById(long id);

    void removeSubTaskById(long id);

    // Получение подзадач эпика
    List<SubTask> getSubtasks(Epic epic);

}
