package manager;

import task.Task;

import java.util.ArrayList;
import java.util.List;

/*
3) проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи;
4) проверьте, что объект Subtask нельзя сделать своим же эпиком;
5) убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
6) проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
7) проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
8) создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
9) убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
*/

public class InMemoryHistoryManager implements HistoryManager {

    private static List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task){
        if(history.size() == 10){
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }

}
