package main.java.manager;

import main.java.exception.ManagerSaveException;
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
    void clearTasks() throws ManagerSaveException;

    void clearEpics() throws ManagerSaveException;

    void clearSubTasks() throws ManagerSaveException;

    // Получение задачи по id
    Task getTaskById(long id);

    Epic getEpicById(long id);

    SubTask getSubTaskById(long id);

    //Создание задачи
    void addTask(Task task) throws ManagerSaveException;

    void addEpic(Epic epic) throws ManagerSaveException;

    void addSubTask(SubTask subTask) throws ManagerSaveException;

    // Обновление задачи
    void updateTask(Task task) throws ManagerSaveException;

    void updateEpic(Epic epic) throws ManagerSaveException;

    void updateSubTask(SubTask subTask) throws ManagerSaveException;

    // Удаление задачи по id
    void removeTaskById(long id) throws ManagerSaveException;

    void removeEpicById(long id) throws ManagerSaveException;

    void removeSubTaskById(long id) throws ManagerSaveException;

    // Получение подзадач эпика
    List<SubTask> getSubtasks(Epic epic);

    public List<Task> getPrioritizedTasks();

}
