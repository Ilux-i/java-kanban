package manager;// Сервис для работы с Задачами

import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Все менеджеры надо свести в один для работы со всеми типами объектов,так как все объекты наследники класса Task
public class TaskManager {
    // Структура данных для хранения Задач
    protected HashMap<Long, Task> tasks = new HashMap<>();


    // Получение всех задач
    public List getListOfTasks(){
        List newTasks = new ArrayList<>();
        for(Task task : tasks.values()){
            newTasks.add(task);
        }
        return newTasks;
    }

    // Удаление всех задач
    public void clearTasks(){
        this.tasks.clear();
    }

    // Получение задачи по id
    public Task getById(long id){
        return this.tasks.get(id);
    }

    //Создание задачи
    public void addTask(Task task) {
        this.tasks.put(task.getId(), task);
    }

    // Обновление задачи
    public void update(Task task) {
        this.tasks.put(task.getId(), task);
    }

    // Удаление задачи по id
    public void removeById(long id) {
        this.tasks.remove(id);
    }
}
