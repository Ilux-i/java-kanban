package manager;

import task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();
    private static final Integer SIZE_HISTORY = 10;

    @Override
    public void add(Task task){
        if(history.size() == SIZE_HISTORY){
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }

//    @Override
//    public Task clone(Task task){
//        Task copy = new Task(task.getName(), task.getDescription());
//        copy.setId(task.getId());
//        copy.setStatus(task.getStatus());
//        return copy;
//    }

}
