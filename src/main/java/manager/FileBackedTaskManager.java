package main.java.manager;

import main.java.exception.ManagerSaveException;
import main.java.status.TaskType;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.io.*;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static String file;
    private static final String HEADER = "id,type,name,status,description,epic";
    TreeSet<Task> sortedSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));

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
        sortedSet.add(task);

        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        sortedSet.add(epic);

        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubTask(SubTask subTask) {
        sortedSet.add(subTask);

        super.addSubTask(subTask);
        epics.get(subTask.getMaster()).checkingTheEpicExecutionTime();
        save();
    }

    // Обновление задачи
    @Override
    public void updateTask(Task task) {
        sortedSet.remove(tasks.get(task.getId()));
        sortedSet.add(task);

        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        sortedSet.remove(epics.get(epic.getId()));
        sortedSet.add(epic);

        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        sortedSet.remove(subTasks.get(subTask.getId()));
        sortedSet.add(subTask);

        super.updateSubTask(subTask);
        epics.get(subTask.getMaster()).checkingTheEpicExecutionTime();
        save();
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

        super.removeSubTaskById(id);
        epics.get(subTasks.get(id).getMaster()).checkingTheEpicExecutionTime();
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
            for (T task : list.values()) {
                br.write(task.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public static FileBackedTaskManager loadFromFile(String file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager();
        taskManager.file = file;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String[] line;
            System.out.println(br.readLine());

            while (br.ready()) {
                line = br.readLine().split(",");
                if (line[0].isEmpty()) {
                    break;
                }

                switch (TaskType.valueOf(line[1])) {
                    case TaskType.TASK:
                        Task task = new Task(line);
                        taskManager.sortedSet.add(task);
                        taskManager.tasks.put(task.getId(), task);
                        break;
                    case TaskType.EPIC:
                        ArrayList<SubTask> list = new ArrayList<>();
                        for (int i = 7; i < line.length; i++) {
                            list.add(taskManager.subTasks.get(Long.parseLong(line[i])));
                        }
                        Epic epic = new Epic(line, list);
                        taskManager.sortedSet.add(epic);
                        taskManager.epics.put(epic.getId(), epic);
                        break;
                    case TaskType.SUBTASK:
                        SubTask subTask = new SubTask(line);
                        taskManager.sortedSet.add(subTask);
                        taskManager.subTasks.put(subTask.getId(), subTask);
                        break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return taskManager;
    }

    public List<Task> getPrioritizedTasks(){
         return sortedSet.stream().toList();
    }

}