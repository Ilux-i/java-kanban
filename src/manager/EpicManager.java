package manager;// Сервис для работы с Эпиками

import status.TaskStatus;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class EpicManager extends TaskManager {

    // Получение подзадач эпика
    public ArrayList<Subtask> getSubtasks(Epic epic) {
        return epic.getSubtasks();
    }

    public void removeById(long id) {
        Epic epic = (Epic) this.tasks.get(id);
        if(epic.getSubtasks() != null) {
            HashMap<Long, Subtask> subtasks =  epic.getSubtasks().getFirst().getTasks();
            for (Subtask subtask : epic.getSubtasks()){
                subtasks.remove(subtask.getId());
            }
        }
        this.tasks.remove(id);
    }

    // Проверка на статус подзадач и изменение статуса эпика
    public static void checkStatus(Epic epic) {
        ArrayList<Subtask> subtasks = epic.getSubtasks();
        int countSubtasks = subtasks.size();
        int countNew = 0;
        int countDone = 0;
        int countInProgress = 0;

        for (Subtask subtask : subtasks) {
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
