package manager;// Сервис для работы с Задачами

import task.Task;

import java.util.HashMap;

public class TaskManager {
    // Структура данных для хранения Задач
    protected static HashMap<Long, Object> tasks = new HashMap<>();

    // Получение всех задач
    public HashMap<Long, Object> getListOfTasks(){
        HashMap<Long, Object> newTasks = new HashMap<>();
        for (Object task : tasks.values()) {
            if (task instanceof Task) {
                newTasks.put(((Task) task).getId(), task);
            }
        }
        return newTasks;
    }

    // Удаление всех задач
    public static void clearTasks(){
        tasks.clear();
    }

    // Получение задачи по id
    public static Task getById(long id){
        return (Task) tasks.get(id);
    }

    //Создание задачи
    public static void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    // Обновление задачи
    public static void update(Task task) {
        tasks.put(task.getId(), task);
    }

    // Удаление задачи по id
    public static void removeById(long id) {
        tasks.remove(id);
    }
}
