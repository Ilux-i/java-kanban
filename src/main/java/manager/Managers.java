package main.java.manager;

import main.java.exception.ManagerSaveException;


public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTaskManager getDefaultFileBackedTaskManager(String file) throws ManagerSaveException {
        return FileBackedTaskManager.loadFromFile(file);
    }
}
