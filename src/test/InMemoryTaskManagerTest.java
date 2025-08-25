package test;

import manager.InMemoryTaskManager;
import manager.Managers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

class InMemoryTaskManagerTest {

    private static InMemoryTaskManager manager;

    @BeforeEach
    public void createManager(){
        manager = (InMemoryTaskManager) Managers.getDefault();
    }

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


//    @Test
//    public void epicCanBeAddedToItself(){
//        Epic epic = new Epic("task1", "description1");
//        manager.addEpic(epic);
//        epic.addSubtask(epic, epic.getId());
//    }
  
}