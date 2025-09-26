package main.java.manager;

import main.java.exception.ManagerSaveException;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class FileBackedTaskManager extends InMemoryTaskManager {

    public FileBackedTaskManager() {
        loadFromFile();
    }

    private static final String FILE = "data.csv";

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
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubTask(SubTask subTask) {
        super.addSubTask(subTask);
        save();
    }

    // Обновление задачи
    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    // Удаление задачи по id
    @Override
    public void removeTaskById(long id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(long id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubTaskById(long id) {
        super.removeSubTaskById(id);
        save();
    }

    private void save() {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(FILE))) {
            String line = "id,type,name,status,description,epic";
            br.write(line + "\n");
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
        try (BufferedWriter br = new BufferedWriter(new FileWriter(FILE, true))) {
            for (T task : list.values()) {
                br.write(task.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    private void loadFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            br.readLine();
            String[] line;
            while (br.ready()) {
                line = br.readLine().split(",");
                if (line[0].isEmpty()) {
                    break;
                }
                switch (line[1]) {
                    case "TASK":
                        Task task = new Task(line);
                        tasks.put(task.getId(), task);
                        break;
                    case "EPIC":
                        ArrayList<SubTask> list = new ArrayList<>();
                        for (int i = 5; i < line.length; i++) {
                            list.add(subTasks.get(Long.parseLong(line[i])));
                        }
                        Epic epic = new Epic(line, list);
                        epics.put(epic.getId(), epic);
                        break;
                    case "SUBTASK":
                        SubTask subTask = new SubTask(line);
                        subTasks.put(subTask.getId(), subTask);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}