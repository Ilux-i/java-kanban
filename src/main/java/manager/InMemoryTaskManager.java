package main.java.manager;// Сервис для работы с Задачами

import main.java.status.TaskStatus;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    // Структура данных для хранения Задач
    protected HashMap<Long, Task> tasks = new HashMap<>();
    protected HashMap<Long, Epic> epics = new HashMap<>();
    protected HashMap<Long, SubTask> subTasks = new HashMap<>();
    protected HistoryManager historyManager = Managers.getDefaultHistory();

    // Получение всех задач
    @Override
    public List<Task> getListOfTasks() {
        return new ArrayList(tasks.values());
    }

    @Override
    public List<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getListOfSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


    // Удаление всех задач
    @Override
    public void clearTasks() {
        for (Task task : tasks.values()) {
            removeTaskById(task.getId());
        }
    }

    @Override
    public void clearEpics() {
        for (Epic epic : epics.values()) {
            removeEpicById(epic.getId());
        }
    }

    @Override
    public void clearSubTasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            epic.setStatus(TaskStatus.NEW);
        }
        for (SubTask subTask : subTasks.values()) {
            removeEpicById(subTask.getId());
        }
    }


    // Получение задачи по id
    @Override
    public Task getTaskById(long id) {
        if (tasks.containsKey(id)) {
            historyManager.add(tasks.get(id));
            return tasks.get(id);
        }
        return null;
    }

    @Override
    public Epic getEpicById(long id) {
        if (epics.containsKey(id)) {
            historyManager.add(epics.get(id));
            return epics.get(id);
        }
        return null;
    }

    @Override
    public SubTask getSubTaskById(long id) {
        if (subTasks.containsKey(id)) {
            historyManager.add(subTasks.get(id));
            return subTasks.get(id);
        }
        return null;
    }


    //Создание задачи
    @Override
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubTask(SubTask subTask) {
        if (subTask.getMaster() != subTask.getId()) {
            Epic epic = epics.get(subTask.getMaster());
            subTasks.put(subTask.getId(), subTask); // добавление в main.java.task
            epic.getSubtasks().add(subTask); // Добавление в subtasks Master
            checkStatus(epic); // Обновление статуса Master
        }
    }

    // Обновление задачи
    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            subTasks.put(subTask.getId(), subTask); // добавление в main.java.task
            checkStatus(epics.get(subTask.getMaster())); // Обновление статуса Master
        }
    }

    // Удаление задачи по id
    @Override
    public void removeTaskById(long id) {
        Task task = tasks.get(id);
        while (historyManager.getHistory().contains(task)) {
            historyManager.remove(task.getId());
        }
        tasks.remove(id);
    }

    @Override
    public void removeEpicById(long id) {
        Epic epic = epics.get(id);
        while (historyManager.getHistory().contains(epic)) {
            historyManager.remove(epic.getId());
        }
        if (epic.getSubtasks() != null) {
            while (!(epic.getSubtasks().isEmpty())) {
                SubTask subTask = epic.getSubtasks().getFirst();
                removeSubTaskById(subTask.getId());
            }
        }
        epics.remove(id);
    }

    @Override
    public void removeSubTaskById(long id) {
        SubTask subTask = subTasks.get(id);
        Epic epic = epics.get(subTask.getMaster());
        epic.getSubtasks().remove(subTask);
        while (historyManager.getHistory().contains(subTask)) {
            historyManager.remove(subTask.getId());
        }
        subTasks.remove(id);
        checkStatus(epic); // Обновление статуса Master
    }


    // Получение подзадач эпика
    @Override
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
            switch (subtask.getStatus()) {
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

        if (countNew == countSubtasks) {
            epic.setStatus(TaskStatus.NEW);
        } else if (countDone == countSubtasks) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

}
