package main.java.manager;

import main.java.exception.ManagerSaveException;
import main.java.status.TaskType;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.io.*;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private String file;
    private static final String HEADER = "id,type,name,status,description,duration,startTime,moreInfo";

    // Удаление всех задач
    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }

    //Создание задачи
    @Override
    public void addTask(Task task) {
        try {
            checkingIntersectionsForSortedSet(task);

            super.addTask(task);
            save();
        } catch (ManagerSaveException e) {
            e.getMessage();
        }

    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
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
            super.addSubTask(subTask);
            epics.get(subTask.getMaster()).checkingTheEpicExecutionTime();
            save();
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

            super.updateTask(task);
            save();
        } catch (ManagerSaveException e) {
            e.getMessage();
            sortedSet.add(task1);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        Task subTask1 = tasks.get(subTask.getId());
        sortedSet.remove(subTask1);
        try {
            checkingIntersectionsForSortedSet(subTask);

            super.updateSubTask(subTask);
            epics.get(subTask.getMaster()).checkingTheEpicExecutionTime();
            save();
        } catch (ManagerSaveException e) {
            e.getMessage();
            sortedSet.add(subTask1);
        }
    }

    // Удаление задачи по id
    @Override
    public void removeTaskById(long id) {
        sortedSet.remove(tasks.get(id));

        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(long id) {
        sortedSet.remove(epics.get(id));

        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubTaskById(long id) {
        sortedSet.remove(subTasks.get(id));

        Epic epic = epics.get(subTasks.get(id).getMaster());
        super.removeSubTaskById(id);
        epic.checkingTheEpicExecutionTime();
        save();
    }

    private void save() {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(file))) {
            br.write(HEADER + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writeTaskToFile(tasks);
            writeTaskToFile(subTasks);
            writeTaskToFile(epics);
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка сохранения данных в файл");
        }
    }

    private <T extends Task> void writeTaskToFile(HashMap<Long, T> list) throws ManagerSaveException {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(file, true))) {
            List<String> lines = list.values().stream()
                    .map(Object::toString)
                    .toList();
            br.write(String.join(",", lines) + System.lineSeparator());
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public static FileBackedTaskManager loadFromFile(String file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager();
        taskManager.file = file;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String[] line;

            br.readLine();

            while (br.ready()) {
                line = br.readLine().split(",");

                switch (TaskType.valueOf(line[1])) {
                    case TaskType.TASK:
                        Task task = new Task(line);
                        taskManager.addTask(task);
                        taskManager.tasks.put(task.getId(), task);
                        break;
                    case TaskType.EPIC:
                        ArrayList<SubTask> list = new ArrayList<>();
                        for (int i = 7; i < line.length; i++) {
                            list.add(taskManager.subTasks.get(Long.parseLong(line[i])));
                        }
                        Epic epic = new Epic(line, list);
                        taskManager.addEpic(epic);
                        taskManager.epics.put(epic.getId(), epic);
                        break;
                    case TaskType.SUBTASK:
                        SubTask subTask = new SubTask(line);
                        taskManager.addSubTask(subTask);
                        taskManager.subTasks.put(subTask.getId(), subTask);
                        break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return taskManager;
    }

}