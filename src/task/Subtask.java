package task;

import manager.SubtaskManager;

import java.util.HashMap;

// Класс отвечающий за реализацию объекта "Эпик"
public class Subtask extends Task {

    private Epic master; // Поле, в котором храниться принадлежность подзадачи
    private HashMap<Long, Subtask> tasks; // Прошу прощения за данную корявость, не нашёл другого способа удалять подзадачи из списка подзадач, удаляя эпик, в который они входят

    public Subtask(String name, String description) {
        super(name, description);

    }

    public void setTasks(HashMap tasks) {
        this.tasks = tasks;
    }
    public HashMap<Long, Subtask> getTasks() {
        return tasks;
    }

    public Epic getMaster() {
        return master;
    }

    public void setMaster(Epic epic) {
        master = epic;
    }

}
