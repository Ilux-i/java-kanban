package manager;// Сервис для работы с Подзадачами

import task.Epic;
import task.Subtask;

import java.util.HashMap;

public class SubtaskManager extends TaskManager {
    // Получение всех задач

    public static HashMap<Long, Subtask> getListOfTasks(Epic epic) {
        return epic.getSubtasks();
    }

    // Удаление всех задач
    public static void clearTasks(Epic epic) {
        epic.getSubtasks().clear();
    }

    // Получение задачи по id
    public static Subtask getById(long id){
        return searchEpic(id).getSubtasks().get(id);
    }

    //Создание задачи
    public static void addTask(Subtask subtask, Epic epic) {
        subtask.setMaster(epic);
        epic.getSubtasks().put(subtask.getId(), subtask);
        EpicManager.addSubtaskInEpic(subtask, epic);
        EpicManager.checkStatus(epic);
    }

    // Обновление задачи по id
    public static void update(Subtask subtask) {
        Epic epic = subtask.getMaster();
        EpicManager.addSubtaskInEpic(subtask, epic);
        EpicManager.checkStatus(epic);
    }

    // Удаление задачи по id
    public static void removeById(long id) {
        Epic epic = searchEpic(id);
        epic.getSubtasks().remove(id);
        EpicManager.checkStatus(epic);
    }

    private static Epic searchEpic(long id) {
        for (Object task : tasks.values()){
            if(task instanceof Epic){
                Epic epic = (Epic) task;
                if(EpicManager.getSubtasks(epic).containsKey(id)){
                    return epic;
                }
            }

        }
        return null;
    }
}
