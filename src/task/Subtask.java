package task;

// Класс отвечающий за реализацию объекта "Эпик"
public class Subtask extends Task {

    private Epic master; // Поле, в котором храниться принадлежность подзадачи

    public Subtask(String name, String description) {
        super(name, description);
    }

    public Epic getMaster() {
        return master;
    }

    public void setMaster(Epic epic) {
        master = epic;
    }
}
