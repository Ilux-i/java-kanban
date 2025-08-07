package manager;// Сервис для работы с Эпиками

import status.TaskStatus;
import task.Epic;
import task.Subtask;

import java.util.HashMap;

public class EpicManager extends TaskManager {

    // Получение всех задач
    @Override
    public HashMap<Long, Object> getListOfTasks(){
        HashMap<Long, Object> newTasks = new HashMap<>();
        for (Object epic : tasks.values()) {
            if (epic instanceof Epic) {
                newTasks.put(((Epic) epic).getId(), epic);
            }
        }
        return newTasks;
    }

    // Получение задачи по id
    public static Epic getById(long id){
        return (Epic) tasks.get(id);
    }

    //Создание задачи
    public static void addTask(Epic epic) {
        tasks.put(epic.getId(), epic);
    }

    // Обновление задачи
    public static void update(Epic epic) {
        tasks.put(epic.getId(), epic);
    }

    // Получение подзадач эпика
    public static HashMap<Long, Subtask> getSubtasks(Epic epic) {
        return epic.getSubtasks();
    }

    // Добавление подзадачи в эпик
    protected static void addSubtaskInEpic(Subtask subtask, Epic epic) {
        epic.getSubtasks().put(subtask.getId(), subtask);
    }

    // Проверка на статус подзадач и изменение статуса эпика
    public static void checkStatus(Epic epic) {
        HashMap<Long, Subtask> subtasks = epic.getSubtasks();
        int countSubtasks = subtasks.size();
        int countNew = 0;
        int countDone = 0;
        int countInProgress = 0;

        for (Subtask subtask : subtasks.values()) {
            switch (subtask.getStatus()){
                case NEW:
                    countNew++;
                    break;
                case DONE:
                    countDone++;
                    break;
                case IN_PROGRESS:
                    countInProgress++;
                    break;
            }
        }

        if(countNew == countSubtasks){
            epic.setStatus(TaskStatus.NEW);
        } else if(countDone == countSubtasks){
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }



}
