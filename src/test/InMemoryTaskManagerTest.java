package test;

import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.util.List;

class InMemoryTaskManagerTest {

    private static TaskManager manager;

    @BeforeEach
    public void createManager(){
        manager = Managers.getDefault();
    }


//  Проверяется, что экземпляры или наследники класса Task равны друг другу, если равен их id;
    @Test
    public void tasksEqualById(){
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);
        Task task2 = manager.getTaskById(task1.getId());

        Assertions.assertEquals(task1, task2);
    }

    @Test
    public void epicsEqualById(){
        Epic task1 = new Epic("task1", "description1");
        manager.addEpic(task1);
        Epic task2 = manager.getEpicById(task1.getId());

        Assertions.assertEquals(task1, task2);
    }

    @Test
    public void subTasksEqualById(){
        Epic epic = new Epic("task1", "description1");
        manager.addEpic(epic);
        SubTask task1 = new SubTask("task1", "description1", epic.getId());
        manager.addSubTask(task1);
        SubTask task2 = manager.getSubTaskById(task1.getId());

        Assertions.assertEquals(task1, task2);
    }


//  Проверяет, что объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    public void epicCannotBeAddedToItself(){
        Epic epic = new Epic("task1", "description1");
        SubTask subTask = new SubTask("task1", "description1", epic.getId());
        subTask.setId(epic.getId());
        manager.addEpic(epic);
        manager.addSubTask(subTask);
        Assertions.assertNull(manager.getSubTaskById(epic.getId()));
    }


//  Проверяет, что объект Subtask нельзя сделать своим же эпиком
    @Test
    public void subTaskCannotBeAddedToItself(){
        SubTask subTask = new SubTask("task1", "description1", 0);
        subTask.setId(0);
        manager.addSubTask(subTask);
        Assertions.assertNull(manager.getSubTaskById(0));
    }


//  Проверяется корректность работы Managers
    @Test
    public void managersReturnInMemoryTaskManager(){
        Assertions.assertEquals(Managers.getDefault().getClass(), InMemoryTaskManager.class);
    }

    @Test
    public void managersReturnInMemoryHistoryManager(){
        Assertions.assertEquals(Managers.getDefaultHistory().getClass(), InMemoryHistoryManager.class);
    }


//  Проверяется, что действительно добавляются задачи разного типа
    @Test
    public void addedTaskIsTask(){
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);

        Assertions.assertEquals(manager.getTaskById(task1.getId()).getClass(), Task.class);
    }

    @Test
    public void addedEpicIsEpic(){
        Epic epic = new Epic("epic1", "description1");
        manager.addEpic(epic);

        Assertions.assertEquals(manager.getEpicById(epic.getId()).getClass(), Epic.class);
    }

    @Test
    public void addedSubTaskIsSubTask(){
        Epic epic = new Epic("epic1", "description1");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("subTask1", "description1", epic.getId());
        manager.addSubTask(subTask1);

        Assertions.assertEquals(manager.getSubTaskById(subTask1.getId()).getClass(), SubTask.class);
    }


    //  Проверяется, что действительно добавляются задачи которые можно найти по Id
    @Test
    public void addedTaskIsFoundById(){
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);

        Task task2 = manager.getTaskById(task1.getId());

        Assertions.assertEquals(task1, task2);
    }

    @Test
    public void addedEpicIsFoundById(){
        Epic epic1 = new Epic("epic1", "description1");
        manager.addEpic(epic1);

        Epic epic2 = manager.getEpicById(epic1.getId());

        Assertions.assertEquals(epic1, epic2);
    }

    @Test
    public void addedSubTaskIsFoundById(){
        Epic epic = new Epic("epic1", "description1");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("subTask1", "description1", epic.getId());
        manager.addSubTask(subTask1);

        SubTask subTask2 = manager.getSubTaskById(subTask1.getId());

        Assertions.assertEquals(subTask1, subTask2);
    }


//  Проверяет, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
    @Test
    public void tasksWithGivenIdAndGeneratedIdDoNotConflictWithinManager(){
        Task task1 = new Task("task1", "description1"); // id сгенерированный
        Task task2 = new Task("task2", "description2");
        task2.setId(100); // id заданный
        manager.addTask(task1);
        manager.addTask(task2);

        Task task3 = manager.getTaskById(task1.getId());
        Task task4 = manager.getTaskById(100);
        Assertions.assertEquals(task1, task3, "сгенерированные id конфликтуют с менеджером при добавлении");
        Assertions.assertEquals(task2, task4, "заданные id конфликтуют с менеджером при добавлении");

        task3.setDescription("description3");
        task4.setDescription("description4");
        manager.updateTask(task3);
        manager.updateTask(task4);
        Assertions.assertEquals(task3, manager.getTaskById(task3.getId()), "сгенерированные id конфликтуют с менеджером при обновлении");
        Assertions.assertEquals(task4, manager.getTaskById(100), "заданные id конфликтуют с менеджером при обновлении");

        Assertions.assertNull(manager.getTaskById(10), "находит задачу с несуществующим id");
    }


//  Тесты, в которых проверяется неизменность задач (по всем полям) при добавлении задач в менеджер
    @Test
    public void taskPersistenceWhenAddingTaskToManager(){
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);
        Task task2 = manager.getTaskById(task1.getId());

        Assertions.assertEquals(task1.getId(), task2.getId());
        Assertions.assertEquals(task1.getName(), task2.getName());
        Assertions.assertEquals(task1.getDescription(), task2.getDescription());
        Assertions.assertEquals(task1.getStatus(), task2.getStatus());
    }

    @Test
    public void epicPersistenceWhenAddingTaskToManager(){
        Epic epic1 = new Epic("epic1", "description1");
        manager.addEpic(epic1);
        Epic epic2 = manager.getEpicById(epic1.getId());

        Assertions.assertEquals(epic1.getId(), epic2.getId());
        Assertions.assertEquals(epic1.getName(), epic2.getName());
        Assertions.assertEquals(epic1.getDescription(), epic2.getDescription());
        Assertions.assertEquals(epic1.getSubtasks(), epic2.getSubtasks());
        Assertions.assertEquals(epic1.getStatus(), epic2.getStatus());
    }

    @Test
    public void subTaskPersistenceWhenAddingTaskToManager(){
        Epic epic1 = new Epic("epic1", "description1");
        manager.addEpic(epic1);
        SubTask subTask1 = new SubTask("subTask1", "description1", epic1.getId());
        manager.addSubTask(subTask1);
        SubTask subTask2 = manager.getSubTaskById(subTask1.getId());

        Assertions.assertEquals(subTask1.getId(), subTask2.getId());
        Assertions.assertEquals(subTask1.getName(), subTask2.getName());
        Assertions.assertEquals(subTask1.getDescription(), subTask2.getDescription());
        Assertions.assertEquals(subTask1.getMaster(), subTask2.getMaster());
        Assertions.assertEquals(subTask1.getStatus(), subTask2.getStatus());
    }


//  Проверяется, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    public void historyManagerSavePreviousVersionOfTaskAndItsData(){
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);

        Task task2 = manager.getTaskById(task1.getId());
        task2.setDescription("description2");
        manager.updateTask(task2);
        Task task3 = manager.getTaskById(task2.getId());
        List<Task> listHistory = manager.getHistory();

        Assertions.assertNotEquals(listHistory.get(0), listHistory.get(1));
    }

    @Test
    public void historyManagerSavePreviousVersionOfEpicAndItsData(){
        Epic epic1 = new Epic("Epic1", "description1");
        manager.addEpic(epic1);

        Epic epic2 = manager.getEpicById(epic1.getId());
        epic2.setDescription("description2");
        manager.updateEpic(epic2);
        Epic task3 = manager.getEpicById(epic2.getId());
        List<Task> listHistory = manager.getHistory();

        Assertions.assertNotEquals(listHistory.get(0), listHistory.get(1));
    }

    @Test
    public void historyManagerSavePreviousVersionOfSubTaskAndItsData(){
        Epic epic = new Epic("Epic1", "description1");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("task1", "description1", epic.getId());
        manager.addSubTask(subTask1);

        SubTask subTask2 = manager.getSubTaskById(subTask1.getId());
        subTask2.setDescription("description2");
        manager.updateTask(subTask2);
        SubTask task3 = manager.getSubTaskById(subTask2.getId());
        List<Task> listHistory = manager.getHistory();

        Assertions.assertNotEquals(listHistory.get(0), listHistory.get(1));
    }
}