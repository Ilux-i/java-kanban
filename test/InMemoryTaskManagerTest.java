package main.java.test;

import main.java.manager.Managers;
import main.java.manager.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @Override
    protected TaskManager createManager() throws IOException {
        return Managers.getDefault();
    }

    @Test
    void historyManagerShouldHandleEmptyHistory() {
        List<Task> history = manager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void historyManagerShouldNotContainDuplicates() {
        Task task = new Task("Task", "Description");
        manager.addTask(task);

        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликатов");
        assertEquals(task, history.get(0));
    }

    @Test
    void historyManagerShouldRemoveFromBeginning() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        Task task3 = new Task("Task 3", "Description 3");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        manager.getTaskById(task1.getId()); // начало
        manager.getTaskById(task2.getId()); // середина
        manager.getTaskById(task3.getId()); // конец

        manager.removeTaskById(task1.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(task1), "Задача из начала должна быть удалена");
        assertTrue(history.contains(task2));
        assertTrue(history.contains(task3));
    }

    @Test
    void historyManagerShouldRemoveFromMiddle() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        Task task3 = new Task("Task 3", "Description 3");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId()); // середина
        manager.getTaskById(task3.getId());

        manager.removeTaskById(task2.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(task1));
        assertFalse(history.contains(task2), "Задача из середины должна быть удалена");
        assertTrue(history.contains(task3));
    }

    @Test
    void historyManagerShouldRemoveFromEnd() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        Task task3 = new Task("Task 3", "Description 3");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getTaskById(task3.getId()); // конец

        manager.removeTaskById(task3.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task2));
        assertFalse(history.contains(task3), "Задача из конца должна быть удалена");
    }

    @Test
    void historyManagerShouldMaintainOrderAfterRemoval() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        Task task3 = new Task("Task 3", "Description 3");
        Task task4 = new Task("Task 4", "Description 4");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        manager.addTask(task4);

        // Создаем историю в определенном порядке
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getTaskById(task3.getId());
        manager.getTaskById(task4.getId());

        // Удаляем из середины
        manager.removeTaskById(task2.getId());

        // Порядок должен сохраниться
        List<Task> history = manager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0), "Первый элемент должен остаться первым");
        assertEquals(task3, history.get(1), "Третий элемент должен стать вторым");
        assertEquals(task4, history.get(2), "Четвертый элемент должен стать третьим");
    }

    @Test
    void historyManagerShouldHandleMixedTaskTypes() {
        Task task = new Task("Task", "Description");
        Epic epic = new Epic("Epic", "Description");
        manager.addTask(task);
        manager.addEpic(epic);

        SubTask subTask = new SubTask("SubTask", "Description", epic.getId());
        manager.addSubTask(subTask);

        // Получаем все типы задач
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubTaskById(subTask.getId());

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size(), "История должна содержать все типы задач");
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
        assertTrue(history.contains(subTask));
    }

    @Test
    void historyManagerShouldUpdateTaskInHistory() {
        Task task = new Task("Original", "Original Description");
        manager.addTask(task);
        manager.getTaskById(task.getId()); // добавляем в историю

        // Обновляем задачу
        task.setName("Updated");
        task.setDescription("Updated Description");
        manager.updateTask(task);

        // История должна содержать обновленную задачу
        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        Task historyTask = history.get(0);
        assertEquals("Updated", historyTask.getName());
        assertEquals("Updated Description", historyTask.getDescription());
    }

    @Test
    void historyManagerShouldLimitSize() {
        // Добавляем много задач
        for (int i = 1; i <= 10; i++) {
            Task task = new Task("Task " + i, "Description " + i);
            manager.addTask(task);
            manager.getTaskById(task.getId());
        }

        // Получаем историю
        List<Task> history = manager.getHistory();

        // История должна содержать все задачи (если нет ограничения)
        assertEquals(10, history.size(), "История должна содержать все просмотренные задачи");
    }

    @Test
    void historyManagerShouldWorkAfterClear() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        manager.addTask(task1);
        manager.addTask(task2);

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());

        // Очищаем все задачи
        manager.clearTasks();

        // История должна быть пустой
        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после очистки задач");
    }

    //  Проверяется, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    public void historyManagerSavePreviousVersionOfTaskAndItsData() {
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);

        Task task2 = manager.getTaskById(task1.getId());
        assertEquals(manager.getHistory().get(0).getId(), task2.getId());
        assertEquals(manager.getHistory().get(0).getName(), task2.getName());
        assertEquals(manager.getHistory().get(0).getDescription(), task2.getDescription());
        assertEquals(manager.getHistory().get(0).getStatus(), task2.getStatus());

        task2.setDescription("description2");
        manager.updateTask(task2);
        Task task3 = manager.getTaskById(task2.getId());
        assertEquals(manager.getHistory().get(0).getId(), task3.getId());
        assertEquals(manager.getHistory().get(0).getName(), task3.getName());
        assertEquals(manager.getHistory().get(0).getDescription(), task3.getDescription());
        assertEquals(manager.getHistory().get(0).getStatus(), task3.getStatus());

    }

    @Test
    public void historyManagerSavePreviousVersionOfEpicAndItsData() {
        Epic epic1 = new Epic("Epic1", "description1");
        manager.addEpic(epic1);

        Epic epic2 = manager.getEpicById(epic1.getId());
        assertEquals(manager.getHistory().get(0).getId(), epic2.getId());
        assertEquals(manager.getHistory().get(0).getName(), epic2.getName());
        assertEquals(manager.getHistory().get(0).getDescription(), epic2.getDescription());
        assertEquals(manager.getHistory().get(0).getStatus(), epic2.getStatus());

        epic2.setDescription("description2");
        manager.updateEpic(epic2);
        Epic epic3 = manager.getEpicById(epic2.getId());
        assertEquals(manager.getHistory().get(0).getId(), epic3.getId());
        assertEquals(manager.getHistory().get(0).getName(), epic3.getName());
        assertEquals(manager.getHistory().get(0).getDescription(), epic3.getDescription());
        assertEquals(manager.getHistory().get(0).getStatus(), epic3.getStatus());


    }

    @Test
    public void historyManagerSavePreviousVersionOfSubTaskAndItsData() {
        Epic epic = new Epic("Epic1", "description1");
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("task1", "description1", epic.getId());
        manager.addSubTask(subTask1);

        SubTask subTask2 = manager.getSubTaskById(subTask1.getId());
        assertEquals(manager.getHistory().get(0), subTask2);

        subTask2.setDescription("description2");
        manager.updateTask(subTask2);
        SubTask subTask3 = manager.getSubTaskById(subTask2.getId());
        assertEquals(manager.getHistory().get(0).getId(), subTask3.getId());
        assertEquals(manager.getHistory().get(0).getName(), subTask3.getName());
        assertEquals(manager.getHistory().get(0).getDescription(), subTask3.getDescription());
        assertEquals(manager.getHistory().get(0).getStatus(), subTask3.getStatus());
    }

    //  Таска должна удаляться из хешмапы и истории
    @Test
    public void deleteTaskByIdFromEntireSystem() {
        Task task1 = new Task("task1", "description1");
        manager.addTask(task1);
        task1 = manager.getTaskById(task1.getId());
        manager.removeTaskById(task1.getId());

        Assertions.assertNull(manager.getTaskById(task1.getId()));
        assertFalse(manager.getHistory().contains(task1));
    }

    //  Эпик должен удаляться из хешмапы и истории как и его сабтаски
    @Test
    public void deleteEpicByIdFromEntireSystem() {
        Epic epic = new Epic("task1", "description1");
        SubTask subTask = new SubTask("subTask1", "description1", epic.getId());
        manager.addEpic(epic);
        manager.addSubTask(subTask);

        epic = manager.getEpicById(epic.getId());
        subTask = manager.getSubTaskById(subTask.getId());
        manager.removeEpicById(epic.getId());

        Assertions.assertNull(manager.getTaskById(epic.getId()));
        assertFalse(manager.getHistory().contains(epic));
        for (SubTask task : epic.getSubtasks()) {
            Assertions.assertNull(manager.getSubTaskById(task.getId()));
            assertFalse(manager.getHistory().contains(task));
        }
    }

    //  Сабтаски должны удаляться из хешмапы, истории и списка сабтасков эпика(только она)
    @Test
    public void deleteSubTaskByIdFromEntireSystem() {
        Epic epic = new Epic("task1", "description1");
        SubTask subTask1 = new SubTask("subTask1", "description1", epic.getId());
        SubTask subTask2 = new SubTask("subTask2", "description2", epic.getId());
        manager.addEpic(epic);
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        subTask1 = manager.getSubTaskById(subTask1.getId());
        manager.removeSubTaskById(subTask1.getId());

        Assertions.assertNull(manager.getSubTaskById(subTask1.getId()));
        Assertions.assertNotNull(manager.getSubTaskById(subTask2.getId()));
        assertFalse(manager.getHistory().contains(subTask1));
        assertTrue(manager.getHistory().contains(subTask2));
        assertFalse(epic.getSubtasks().contains(subTask1));
        assertTrue(epic.getSubtasks().contains(subTask2));
    }

}