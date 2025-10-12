package main.java.manager;// Сервис для работы с Задачами

import main.java.exception.ManagerSaveException;
import main.java.status.TaskStatus;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    // Структура данных для хранения Задач
    protected HashMap<Long, Task> tasks = new HashMap<>();
    protected HashMap<Long, Epic> epics = new HashMap<>();
    protected HashMap<Long, SubTask> subTasks = new HashMap<>();
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected TreeSet<Task> sortedSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));


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
        new ArrayList<>(tasks.values()).stream()
                .map(Task::getId)
                .forEach(this::removeTaskById);
    }

    @Override
    public void clearEpics() {
        new ArrayList<>(epics.values()).stream()
                .map(Epic::getId)
                .forEach(this::removeEpicById);
    }

    @Override
    public void clearSubTasks() {
        // Очищаем подзадачи у всех эпиков и сбрасываем статус
        epics.values().stream()
                .peek(epic -> epic.getSubtasks().clear())
                .forEach(epic -> epic.setStatus(TaskStatus.NEW));

        // Удаляем подзадачи из мапы (исправлено: removeSubTaskById вместо removeEpicById)
        new ArrayList<>(subTasks.values()).stream()
                .map(SubTask::getId)
                .forEach(this::removeSubTaskById);
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
        try {
            checkingIntersectionsForSortedSet(task);

            tasks.put(task.getId(), task);
        } catch (ManagerSaveException e) {
            e.getMessage();
        }

    }

    @Override
    public void addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubTask(SubTask subTask) {
        try {
            checkingIntersectionsForSortedSet(subTask);

            if (subTask.getMaster() == subTask.getId()) {
                throw new ManagerSaveException("Подзадача не может содержаться сама в себе.");
            }
            if (!epics.containsKey(subTask.getMaster())) {
                throw new ManagerSaveException("Подзадача не может быть добавлена к несуществующему эпику");
            }
            Epic epic = epics.get(subTask.getMaster());
            subTasks.put(subTask.getId(), subTask); // добавление в main.java.task
            epic.getSubtasks().add(subTask); // Добавление в subtasks Master
            checkStatus(epic); // Обновление статуса Master
            epics.get(subTask.getMaster()).checkingTheEpicExecutionTime();
        } catch (ManagerSaveException e) {
            e.getMessage();
        }
    }


    // Обновление задачи
    @Override
    public void updateTask(Task task) {
        Task task1 = tasks.get(task.getId());
        sortedSet.remove(task1);
        try {
            checkingIntersectionsForSortedSet(task);

            if (tasks.containsKey(task.getId())) {
                tasks.put(task.getId(), task);
            }
        } catch (ManagerSaveException e) {
            e.getMessage();
            sortedSet.add(task1);
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
        Task subTask1 = tasks.get(subTask.getId());
        sortedSet.remove(subTask1);
        try {
            checkingIntersectionsForSortedSet(subTask);

            if (subTasks.containsKey(subTask.getId())) {
                subTasks.put(subTask.getId(), subTask); // добавление в main.java.task
                checkStatus(epics.get(subTask.getMaster())); // Обновление статуса Master
            }
        } catch (ManagerSaveException e) {
            e.getMessage();
            sortedSet.add(subTask1);
        }
    }


    // Удаление задачи по id
    @Override
    public void removeTaskById(long id) {
        Task task = tasks.get(id);
        sortedSet.remove(task);
        while (historyManager.getHistory().contains(task)) {
            historyManager.remove(task.getId());
        }
        tasks.remove(id);
    }

    @Override
    public void removeEpicById(long id) {
        Epic epic = epics.get(id);
        sortedSet.remove(epic);
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
        sortedSet.remove(subTasks.get(id));
        Epic epic = epics.get(subTask.getMaster());
        epic.getSubtasks().remove(subTask);
        epic.checkingTheEpicExecutionTime();
        while (historyManager.getHistory().contains(subTask)) {
            historyManager.remove(subTask.getId());
        }
        subTasks.remove(id);
        checkStatus(epic); // Обновление статуса Master
    }


    // Получение подзадач эпика
    @Override
    public List<SubTask> getSubtasks(Epic epic) {
        return epic.getSubtasks().stream()
                .map(subTask -> subTasks.get(subTask.getId()))
                .collect(Collectors.toList());
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

    public List<Task> getPrioritizedTasks() {
        return sortedSet.stream().toList();
    }

    //    Проверяет нет ли пересечений по времени выполнения задач
    private boolean checkingIntersections(Task task_1, Task task_2) {
        if (task_1.getEndTime().isBefore(task_2.getStartTime())
                || task_1.getEndTime().isEqual(task_2.getStartTime())) {
            return true;
        } else if (task_2.getEndTime().isBefore(task_1.getStartTime())
                || task_2.getEndTime().isEqual(task_1.getStartTime())) {
            return true;
        } else {
            return false;
        }

    }

    //    Проверяет нет ли пересечений по времени выполнения задачи с остальными из sortedSet
    public void checkingIntersectionsForSortedSet(Task task) throws ManagerSaveException {
        if (task.getDuration() != null) {
            List<Boolean> fall = sortedSet.stream()
                    .map(task1 -> checkingIntersections(task, task1))
                    .toList();
            if (!fall.contains(false)) {
                sortedSet.add(task);
            } else {
                throw new ManagerSaveException("Задача не добавлена, так как пересекается по времени выполнения с другими задачами.");
            }
        }
    }

}