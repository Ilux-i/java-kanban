package main.java.test;

import main.java.manager.FileBackedTaskManager;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;
    private Path testFile;

    @Override
    protected FileBackedTaskManager createManager() throws IOException {
        return FileBackedTaskManager.loadFromFile(testFile.toString());
    }

    @BeforeEach
    @Override
    void reset() throws IOException {
        testFile = tempDir.resolve("data.csv");
        Files.createFile(testFile);
        manager = createManager();
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile.toString());

        assertTrue(loadedManager.getListOfTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getListOfEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getListOfSubTasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        Task task = new Task("Task 1", "Description 1");
        Epic epic = new Epic("Epic 1", "Description epic");
        SubTask subTask = new SubTask("SubTask 1", "Description subtask", epic.getId());
        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubTask(subTask);


        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile.toString());

        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertTaskEquals(task, loadedTask);

        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEpicEquals(epic, loadedEpic);

        SubTask loadedSubTask = loadedManager.getSubTaskById(subTask.getId());
        assertSubTaskEquals(subTask, loadedSubTask);

    }

    @Test
    void shouldLoadMultipleTasksFromFile() throws IOException {
        String testData = """
                id,type,name,status,description,duration,startTime,moreInfo
                1,TASK,Task_1,NEW,Description 1,null,null
                3,SUBTASK,SubTask_1,NEW,Description subtask,null,null,2
                2,EPIC,Epic_1,NEW,Description epic,null,null,3
                """;
        Files.writeString(testFile, testData);


        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile.toString());

        assertEquals(1, loadedManager.getListOfTasks().size(), "Должна быть 1 задача");
        assertEquals(1, loadedManager.getListOfEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loadedManager.getListOfSubTasks().size(), "Должна быть 1 подзадача");

        Task task = loadedManager.getTaskById(1);
        assertEquals("Task_1", task.getName());

        SubTask subTask = loadedManager.getSubTaskById(3);
        assertEquals(2, subTask.getMaster());
    }

    @Test
    void shouldNotAllowTasksWithOverlappingIntervals() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(2)); // 10:00 - 12:00

        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0)); // 11:00 - 13:00
        task2.setDuration(Duration.ofHours(2));

        manager.addTask(task1);
        manager.addTask(task2);

        assertEquals(1, manager.getListOfTasks().size(), "Должна быть добавлена только одна задача");
        assertNotNull(manager.getTaskById(task1.getId()), "Первая задача должна быть добавлена");
        assertNull(manager.getTaskById(task2.getId()), "Вторая задача не должна быть добавлена из-за пересечения");
    }

    @Test
    void shouldAllowTasksWithNonOverlappingIntervals() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1)); // 10:00 - 11:00

        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0)); // 12:00 - 13:00
        task2.setDuration(Duration.ofHours(1));

        manager.addTask(task1);
        manager.addTask(task2);

        assertEquals(2, manager.getListOfTasks().size(), "Обе задачи должны быть добавлены");
        assertNotNull(manager.getTaskById(task1.getId()), "Первая задача должна быть добавлена");
        assertNotNull(manager.getTaskById(task2.getId()), "Вторая задача должна быть добавлена");
    }

    @Test
    void shouldNotAllowSubTasksWithOverlappingIntervals() {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        subTask1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subTask1.setDuration(Duration.ofHours(2)); // 10:00 - 12:00

        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        subTask2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0)); // 11:00 - 13:00
        subTask2.setDuration(Duration.ofHours(2));

        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2); // Должна быть отклонена

        assertEquals(1, manager.getListOfSubTasks().size(), "Должна быть добавлена только одна подзадача");
        assertNotNull(manager.getSubTaskById(subTask1.getId()), "Первая подзадача должна быть добавлена");
        assertNull(manager.getSubTaskById(subTask2.getId()), "Вторая подзадача не должна быть добавлена");
    }

    @Test
    void shouldAllowTasksWithoutTimeIntervals() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");

        manager.addTask(task1);
        manager.addTask(task2);

        assertEquals(2, manager.getListOfTasks().size(), "Обе задачи без времени должны быть добавлены");
        assertNotNull(manager.getTaskById(task1.getId()));
        assertNotNull(manager.getTaskById(task2.getId()));
    }

    @Test
    void shouldReturnPrioritizedTasksInCorrectOrder() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        task1.setDuration(Duration.ofHours(1));

        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task2.setDuration(Duration.ofHours(1));

        manager.addTask(task1);
        manager.addTask(task2);

        var prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size(), "Должно быть 2 задачи в приоритетном списке");
        assertEquals(task2, prioritizedTasks.get(0), "Первой должна быть задача с более ранним временем");
        assertEquals(task1, prioritizedTasks.get(1), "Второй должна быть задача с более поздним временем");
    }

    @Test
    void shouldHandleComplexIntervalScenarios() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
        task1.setDuration(Duration.ofHours(3)); // 9:00 - 12:00

        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 13, 0)); // 13:00 - 14:00
        task2.setDuration(Duration.ofHours(1));

        Task task3 = new Task("Task 3", "Description 3");
        task3.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0)); // 10:00 - 11:00 - пересекается с task1
        task3.setDuration(Duration.ofHours(1));

        manager.addTask(task1);
        manager.addTask(task2); // Должна добавиться
        manager.addTask(task3); // Не должна добавиться

        assertEquals(2, manager.getListOfTasks().size(), "Должны быть добавлены только 2 задачи");
        assertNotNull(manager.getTaskById(task1.getId()));
        assertNotNull(manager.getTaskById(task2.getId()));
        assertNull(manager.getTaskById(task3.getId()));
    }

    private void assertTaskEquals(Task expected, Task actual) {
        assertEquals(expected.getId(), actual.getId(), "ID задачи не совпадает");
        assertEquals(expected.getName(), actual.getName(), "Название задачи не совпадает");
        assertEquals(expected.getDescription(), actual.getDescription(), "Описание задачи не совпадает");
        assertEquals(expected.getStatus(), actual.getStatus(), "Статус задачи не совпадает");
    }

    private void assertEpicEquals(Epic expected, Epic actual) {
        assertEquals(expected.getId(), actual.getId(), "ID эпика не совпадает");
        assertEquals(expected.getName(), actual.getName(), "Название эпика не совпадает");
        assertEquals(expected.getDescription(), actual.getDescription(), "Описание эпика не совпадает");
        assertEquals(expected.getStatus(), actual.getStatus(), "Статус эпика не совпадает");
        assertEquals(expected.getSubtasks().size(), actual.getSubtasks().size(), "Количество подзадач не совпадает");
    }

    private void assertSubTaskEquals(SubTask expected, SubTask actual) {
        assertEquals(expected.getId(), actual.getId(), "ID подзадачи не совпадает");
        assertEquals(expected.getName(), actual.getName(), "Название подзадачи не совпадает");
        assertEquals(expected.getDescription(), actual.getDescription(), "Описание подзадачи не совпадает");
        assertEquals(expected.getStatus(), actual.getStatus(), "Статус подзадачи не совпадает");
        assertEquals(expected.getMaster(), actual.getMaster(), "Родительский эпик не совпадает");
    }
}