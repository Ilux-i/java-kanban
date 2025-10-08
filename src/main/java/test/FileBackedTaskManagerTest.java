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

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;
    private FileBackedTaskManager manager;
    private Path testFile;

    @BeforeEach
    void reset() throws IOException {
        testFile = tempDir.resolve("data.csv");
        Files.createFile(testFile);
        manager = FileBackedTaskManager.loadFromFile(testFile.toString());
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