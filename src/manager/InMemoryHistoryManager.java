package manager;

import task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task){
        if(history.size() == 10){
            history.removeFirst();
        }
        history.add(new Task(task.getName(), task.getDescription()));
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }

}
