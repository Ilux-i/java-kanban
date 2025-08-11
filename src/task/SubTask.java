package task;

// Класс отвечающий за реализацию объекта "Эпик"
public class SubTask extends Task {

    private long idMaster; // Поле, в котором храниться принадлежность подзадачи

    public SubTask(String name, String description) {
        super(name, description);

    }

    public long getMaster() {
        return idMaster;
    }

    public void setMaster(long id) {
        idMaster = id;
    }

}
