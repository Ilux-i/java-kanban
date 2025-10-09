package main.java.task;

import java.time.Duration;
import java.time.LocalDateTime;

// Класс отвечающий за реализацию объекта "Эпик"
public class SubTask extends Task {

    private long idMaster; // Поле, в котором храниться принадлежность подзадачи

    public SubTask(String name, String description, long idMaster) {
        super(name, description);
        this.idMaster = idMaster;
    }

    public SubTask(String name, String description, long idMaster, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
        this.idMaster = idMaster;
    }

    public SubTask(String[] list) {
        super(list);
        this.idMaster = Long.parseLong(list[7]);
    }

    public long getMaster() {
        return idMaster;
    }

    public void setMaster(long id) {
        idMaster = id;
    }

    public String toString() {
        String str = super.toString();
        str += "," + String.valueOf(this.getMaster());
        return str;
    }

}
