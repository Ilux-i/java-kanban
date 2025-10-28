package main.java.test;

import main.java.exception.ManagerSaveException;
import main.java.manager.InMemoryHistoryManager;
import main.java.manager.InMemoryTaskManager;
import main.java.manager.Managers;
import main.java.manager.TaskManager;
import main.java.status.TaskStatus;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager() throws IOException, ManagerSaveException;

    @BeforeEach
    void reset() throws IOException, ManagerSaveException {
        manager = createManager();
    }

    //  Проверяется, что экземпляры или наследники класса Task равны друг другу, если равен их id;
    @Test
    void tasksEqualById() throws ManagerSaveException {
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);
        Task task2 = manager.getTaskById(task1.getId());

        assertEquals(task1, task2);
    }

    @Test
    void epicsEqualById() throws ManagerSaveException {
        Epic task1 = new Epic("task1", "description1");
        manager.addEpic(task1);
        Epic task2 = manager.getEpicById(task1.getId());

        assertEquals(task1, task2);
    }

    @Test
    void subTasksEqualById() throws ManagerSaveException {
        Epic epic = new Epic("task1", "description1");
        manager.addEpic(epic);
        SubTask task1 = new SubTask("task1", "description1", epic.getId());
        manager.addSubTask(task1);
        SubTask task2 = manager.getSubTaskById(task1.getId());

        assertEquals(task1, task2);
    }


    //  Проверяет, что объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    void epicCannotBeAddedToItself() throws ManagerSaveException {
        Epic epic = new Epic("task1", "description1");
        SubTask subTask = new SubTask("task1", "description1", epic.getId());
        subTask.setId(epic.getId());
        try {
            manager.addEpic(epic);
            manager.addSubTask(subTask);
            assertNull(manager.getSubTaskById(epic.getId()));
        } catch (ManagerSaveException e) {
            if (e.getClass() == ManagerSaveException.class) {
                assertTrue(true);
            } else {
                assertTrue(false);
            }
        }
    }


    //  Проверяет, что объект Subtask нельзя сделать своим же эпиком
    @Test
    void subTaskCannotBeAddedToItself() throws ManagerSaveException {
        SubTask subTask = new SubTask("task1", "description1", 0);
        subTask.setId(0);
        try {
            manager.addSubTask(subTask);
        } catch (ManagerSaveException e) {
            if (e.getClass() == ManagerSaveException.class) {
                assertTrue(true);
            } else {
                assertTrue(false);
            }
        }
    }


    //  Проверяется корректность работы Managers
    @Test
    void managersReturnInMemoryTaskManager() {
        assertEquals(Managers.getDefault().getClass(), InMemoryTaskManager.class);
    }

    @Test
    public void managersReturnInMemoryHistoryManager() {
        assertEquals(Managers.getDefaultHistory().getClass(), InMemoryHistoryManager.class);
    }


    //  Проверяется, что действительно добавляются задачи разного типа
    @Test
    void addedTaskIsTask() throws ManagerSaveException {
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);

        assertEquals(manager.getTaskById(task1.getId()).getClass(), Task.class);
    }

    @Test
    void addedEpicIsEpic() throws ManagerSaveException {
        Epic epic = new Epic("epic1", "description1");
        manager.addEpic(epic);

        assertEquals(manager.getEpicById(epic.getId()).getClass(), Epic.class);
    }

    @Test
    void addedSubTaskIsSubTask() throws ManagerSaveException {
        Epic epic = new Epic("epic1", "description1");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("subTask1", "description1", epic.getId());
        manager.addSubTask(subTask1);

        assertEquals(manager.getSubTaskById(subTask1.getId()).getClass(), SubTask.class);
    }


    //  Проверяется, что действительно добавляются задачи которые можно найти по Id
    @Test
    void addedTaskIsFoundById() throws ManagerSaveException {
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);

        Task task2 = manager.getTaskById(task1.getId());

        assertEquals(task1, task2);
    }

    @Test
    void addedEpicIsFoundById() throws ManagerSaveException {
        Epic epic1 = new Epic("epic1", "description1");
        manager.addEpic(epic1);

        Epic epic2 = manager.getEpicById(epic1.getId());

        assertEquals(epic1, epic2);
    }

    @Test
    void addedSubTaskIsFoundById() throws ManagerSaveException {
        Epic epic = new Epic("epic1", "description1");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("subTask1", "description1", epic.getId());
        manager.addSubTask(subTask1);

        SubTask subTask2 = manager.getSubTaskById(subTask1.getId());

        assertEquals(subTask1, subTask2);
    }


    //  Проверяет, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
    @Test
    void tasksWithGivenIdAndGeneratedIdDoNotConflictWithinManager() throws ManagerSaveException {
        Task task1 = new Task("task1", "description1"); // id сгенерированный
        Task task2 = new Task("task2", "description2");
        task2.setId(100); // id заданный
        manager.addTask(task1);
        manager.addTask(task2);

        Task task3 = manager.getTaskById(task1.getId());
        Task task4 = manager.getTaskById(100);
        assertEquals(task1, task3, "сгенерированные id конфликтуют с менеджером при добавлении");
        assertEquals(task2, task4, "заданные id конфликтуют с менеджером при добавлении");

        task3.setDescription("description3");
        task4.setDescription("description4");
        manager.updateTask(task3);
        manager.updateTask(task4);
        assertEquals(task3, manager.getTaskById(task3.getId()), "сгенерированные id конфликтуют с менеджером при обновлении");
        assertEquals(task4, manager.getTaskById(100), "заданные id конфликтуют с менеджером при обновлении");

        assertNull(manager.getTaskById(10), "находит задачу с несуществующим id");
    }


    //  Тесты, в которых проверяется неизменность задач (по всем полям) при добавлении задач в менеджер
    @Test
    void taskPersistenceWhenAddingTaskToManager() throws ManagerSaveException {
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);
        Task task2 = manager.getTaskById(task1.getId());

        assertEquals(task1.getId(), task2.getId());
        assertEquals(task1.getName(), task2.getName());
        assertEquals(task1.getDescription(), task2.getDescription());
        assertEquals(task1.getStatus(), task2.getStatus());
    }

    @Test
    void epicPersistenceWhenAddingTaskToManager() throws ManagerSaveException {
        Epic epic1 = new Epic("epic1", "description1");
        manager.addEpic(epic1);
        Epic epic2 = manager.getEpicById(epic1.getId());

        assertEquals(epic1.getId(), epic2.getId());
        assertEquals(epic1.getName(), epic2.getName());
        assertEquals(epic1.getDescription(), epic2.getDescription());
        assertEquals(epic1.getSubtasks(), epic2.getSubtasks());
        assertEquals(epic1.getStatus(), epic2.getStatus());
    }

    @Test
    void subTaskPersistenceWhenAddingTaskToManager() throws ManagerSaveException {
        Epic epic1 = new Epic("epic1", "description1");
        manager.addEpic(epic1);
        SubTask subTask1 = new SubTask("subTask1", "description1", epic1.getId());
        manager.addSubTask(subTask1);
        SubTask subTask2 = manager.getSubTaskById(subTask1.getId());

        assertEquals(subTask1.getId(), subTask2.getId());
        assertEquals(subTask1.getName(), subTask2.getName());
        assertEquals(subTask1.getDescription(), subTask2.getDescription());
        assertEquals(subTask1.getMaster(), subTask2.getMaster());
        assertEquals(subTask1.getStatus(), subTask2.getStatus());
    }

    @Test
    void subTaskShouldBeAddedToEpic() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask = new SubTask("SubTask", "Description", epic.getId());

        manager.addSubTask(subTask);

        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertEquals(1, retrievedEpic.getSubtasks().size());
        assertEquals(subTask, retrievedEpic.getSubtasks().get(0));
    }

    @Test
    void subTaskShouldReferenceCorrectEpic() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask = new SubTask("SubTask", "Description", epic.getId());

        manager.addSubTask(subTask);

        SubTask retrievedSubTask = manager.getSubTaskById(subTask.getId());
        assertEquals(epic.getId(), retrievedSubTask.getMaster());
    }

    @Test
    void shouldNotAddSubTaskWithInvalidEpic() throws ManagerSaveException {
        SubTask invalidSubTask = new SubTask("Invalid", "Desc", 999L);
        try {
            manager.addSubTask(invalidSubTask);
        } catch (ManagerSaveException e) {
            if (e.getClass() == ManagerSaveException.class) {
                assertTrue(true);
            } else {
                assertTrue(false);
            }
        }
    }

    @Test
    void removingSubTaskShouldRemoveItFromEpic() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        manager.removeSubTaskById(subTask1.getId());

        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertEquals(1, retrievedEpic.getSubtasks().size());
        assertFalse(retrievedEpic.getSubtasks().contains(subTask1));
    }

    @Test
    void removingEpicShouldRemoveAllItsSubTasks() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        manager.removeEpicById(epic.getId());

        assertNull(manager.getSubTaskById(subTask1.getId()));
        assertNull(manager.getSubTaskById(subTask2.getId()));
    }

    @Test
    void epicStatusShouldBeNewWhenNoSubTasks() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubTasksNew() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        subTask1.setStatus(TaskStatus.NEW);
        subTask2.setStatus(TaskStatus.NEW);
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubTasksDone() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        subTask1.setStatus(TaskStatus.DONE);
        subTask2.setStatus(TaskStatus.DONE);
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, retrievedEpic.getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubTasksMixed() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        subTask1.setStatus(TaskStatus.NEW);
        subTask2.setStatus(TaskStatus.DONE);
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubTaskInProgress() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        subTask2.setStatus(TaskStatus.NEW);
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
    }

    @Test
    void updatingSubTaskShouldUpdateEpicStatus() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask = new SubTask("SubTask", "Description", epic.getId());
        subTask.setStatus(TaskStatus.NEW);
        manager.addSubTask(subTask);

        subTask.setStatus(TaskStatus.DONE);
        manager.updateSubTask(subTask);

        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, retrievedEpic.getStatus());
    }

    @Test
    void epicStatusShouldUpdateWhenSubTaskRemoved() throws ManagerSaveException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);
        SubTask subTask = new SubTask("SubTask", "Description", epic.getId());
        subTask.setStatus(TaskStatus.DONE);
        manager.addSubTask(subTask);

        manager.removeSubTaskById(subTask.getId());

        Epic retrievedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
    }

}
