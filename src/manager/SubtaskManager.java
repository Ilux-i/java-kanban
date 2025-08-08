package manager;// Сервис для работы с Подзадачами

import task.Epic;
import task.Subtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubtaskManager extends TaskManager {


    // Удаление всех задач
    public void clearTasks(Epic epic) {
        for (Subtask subtask : epic.getSubtasks()) {
            removeById(subtask.getId());
        }
    }


    //Создание задачи
    public void addTask(Subtask subtask, Epic epic) {
        subtask.setMaster(epic);
        subtask.setTasks(this.tasks);
        this.tasks.put(subtask.getId(), subtask); // добавление в task
        epic.getSubtasks().add(subtask); // Добавление в subtasks Master
        EpicManager.checkStatus(epic); // Обновление статуса Master
    }

    // Обновление задачи по id
    public void update(Subtask subtask) {
        Epic epic = subtask.getMaster();
        this.tasks.put(subtask.getId(), subtask); // добавление в task
        ArrayList<Subtask> arr = epic.getSubtasks(); // Добавление в subtasks Master
        for(int i = 0; i < arr.size(); i++){
            if(arr.get(i).getId() == subtask.getId()){
                epic.getSubtasks().set(i, subtask);
            }
        }
        EpicManager.checkStatus(epic); // Обновление статуса Master
    }

    // Удаление задачи по id
    public void removeById(long id) {
        Subtask subtask = (Subtask) tasks.get(id);
        subtask.getMaster().getSubtasks().remove(subtask);
        tasks.remove(id);
        EpicManager.checkStatus(subtask.getMaster()); // Обновление статуса Master
    }

    public void clearByEpic(Epic epic) {
        for (Subtask subtask : epic.getSubtasks()) {
            tasks.remove(subtask.getId());
        }
    }



}
