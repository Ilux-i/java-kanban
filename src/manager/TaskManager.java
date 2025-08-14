package manager;// Сервис для работы с Задачами

import status.TaskStatus;
import task.Epic;
import task.SubTask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    // Структура данных для хранения Задач
    protected HashMap<Long, Task> tasks = new HashMap<>();
    protected HashMap<Long, Epic> epics = new HashMap<>();
    protected HashMap<Long, SubTask> subTasks = new HashMap<>();


    // Получение всех задач
    public List<Task> getListOfTasks(){
        return new ArrayList(tasks.values());
    }

    public List<Epic> getListOfEpics(){
        return new ArrayList<>(epics.values());
    }

    public List<SubTask> getListOfSubTasks(){
        return new ArrayList<>(subTasks.values());
    }


    // Удаление всех задач
    public void clearTasks(){
        this.tasks.clear();
    }

    public void clearEpics(){
        for(Epic epic : epics.values()){
            removeEpicById(epic.getId());
        }
    }

    public void clearSubTasks(){
        for(Epic epic : epics.values()){
            epic.getSubtasks().clear();
            epic.setStatus(TaskStatus.NEW);
        }
        this.subTasks.clear();
    }


    // Получение задачи по id
    public Task getTaskById(long id){
        return tasks.get(id);
    }

    public Epic getEpicById(long id){
        return epics.get(id);
    }

    public SubTask getSubTaskById(long id){
        return subTasks.get(id);
    }


    //Создание задачи
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void addSubTask(SubTask subTask) {
        Epic epic = epics.get(subTask.getMaster());
        subTask.setMaster(epic.getId());
        subTasks.put(subTask.getId(), subTask); // добавление в task
        epic.getSubtasks().add(subTask); // Добавление в subtasks Master
        checkStatus(epic); // Обновление статуса Master
    }


    // Обновление задачи
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void updateSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask); // добавление в task
        checkStatus(epics.get(subTask.getMaster())); // Обновление статуса Master
    }


    // Удаление задачи по id
    public void removeTaskById(long id) {
        tasks.remove(id);
    }

    public void removeEpicById(long id) {
        Epic epic = epics.get(id);
        if(epic.getSubtasks() != null) {
            for (SubTask subTask : epic.getSubtasks()){
                subTasks.remove(subTask.getId());
            }
        }
        epics.remove(id);
    }

    public void removeSubTaskById(long id) {
        SubTask subTask = subTasks.get(id);
        Epic epic = epics.get(subTask.getMaster());
        epic.getSubtasks().remove(subTask);
        subTasks.remove(id);
        checkStatus(epic); // Обновление статуса Master
    }


    // Получение подзадач эпика
    public List<SubTask> getSubtasks(Epic epic) {
        ArrayList<SubTask> newSubTasks = new ArrayList<>();
        for (SubTask subTask : epic.getSubtasks()) {
            newSubTasks.add(subTasks.get(subTask.getId()));
        }
        return newSubTasks;
    }


    // Проверка на статус подзадач и изменение статуса эпика
    private static void checkStatus(Epic epic) {
        ArrayList<SubTask> subtasks = epic.getSubtasks();
        int countSubtasks = subtasks.size();
        int countNew = 0;
        int countDone = 0;
        int countInProgress = 0;

        for (SubTask subtask : subtasks) {
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
