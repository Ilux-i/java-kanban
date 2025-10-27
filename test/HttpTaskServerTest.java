package main.java.test;

import com.google.gson.GsonBuilder;
import main.java.exception.ManagerSaveException;
import main.java.manager.FileBackedTaskManager;
import main.java.server.HttpTaskServer;
import main.java.server.handler.adapter.DurationTypeAdapter;
import main.java.server.handler.adapter.EpicTypeAdapter;
import main.java.server.handler.adapter.LocalDateTimeTypeAdapter;
import main.java.server.handler.adapter.SubTaskTypeAdapter;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private static FileBackedTaskManager manager;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080";
    private final Gson taskGson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();
    private final Gson epicGson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(SubTask.class, new EpicTypeAdapter())
            .setPrettyPrinting()
            .create();
    private final Gson subtaskGson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(SubTask.class, new SubTaskTypeAdapter())
            .setPrettyPrinting()
            .create();

    private final String testFileName = "data_test.csv";

    @TempDir
    Path tempDir;
    private Path testFile;

    @BeforeEach
    void reset() throws IOException, ManagerSaveException {
        // Очищаем тестовый файл перед каждым тестом
        testFile = tempDir.resolve(testFileName);
        Files.createFile(testFile);

        // Создаем новый FileBackedTaskManager с тестовым файлом
        HttpTaskServer.start(testFile.toString());
        manager = HttpTaskServer.manager;
        client = HttpClient.newHttpClient();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        HttpTaskServer.stop();

        // Очищаем тестовый файл после каждого теста
        try {
            Path testFile = Paths.get(testFileName);
            if (Files.exists(testFile)) {
                Files.delete(testFile);
            }
        } catch (IOException e) {
            System.err.println("Не удалось удалить тестовый файл: " + e.getMessage());
        }
    }

    // ===== TASK TESTS =====
    @Test
    void testCreateAndRetrieveTask() throws IOException, InterruptedException {
        // Создаем задачу
        JsonObject taskJson = new JsonObject();
        taskJson.addProperty("name", "Test Task");
        taskJson.addProperty("description", "Test Description");

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskGson.toJson(taskJson)))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        // Получаем список задач
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        Task[] tasks = taskGson.fromJson(getResponse.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("Test Task", tasks[0].getName());

        // Проверяем через manager
        assertEquals(1, manager.getListOfTasks().size());
    }

    @Test
    void testCreateTaskWithTime() throws IOException, InterruptedException {
        JsonObject taskJson = new JsonObject();
        taskJson.addProperty("name", "Timed Task");
        taskJson.addProperty("description", "Task with time");
        taskJson.addProperty("duration", "PT2H");
        taskJson.addProperty("startTime", "2023-01-01T10:00:00");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskGson.toJson(taskJson)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        // Проверяем, что задача сохранилась в manager
        assertEquals(1, manager.getListOfTasks().size());
        Task task = manager.getListOfTasks().get(0);
        assertEquals("Timed Task", task.getName());
        assertEquals(Duration.ofHours(2), task.getDuration());
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException, ManagerSaveException {
        // Сначала создаем задачу
        Task task = new Task("Original Task", "Original Description");
        manager.addTask(task);
        long taskId = task.getId();

        // Обновляем задачу через HTTP
        JsonObject updateJson = new JsonObject();
        updateJson.addProperty("id", taskId);
        updateJson.addProperty("name", "Updated Task");
        updateJson.addProperty("description", "Updated Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskGson.toJson(updateJson)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        // Проверяем обновление через manager
        Task updatedTask = manager.getTaskById(taskId);
        assertEquals("Updated Task", updatedTask.getName());
        assertEquals("Updated Description", updatedTask.getDescription());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException, ManagerSaveException {
        Task task = new Task("Test Task", "Test Description");
        manager.addTask(task);
        long taskId = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        // Проверяем через manager
        assertNull(manager.getTaskById(taskId));
        assertEquals(0, manager.getListOfTasks().size());
    }

    // ===== EPIC TESTS =====
    @Test
    void testCreateAndRetrieveEpic() throws IOException, InterruptedException {
        JsonObject epicJson = new JsonObject();
        epicJson.addProperty("name", "Test Epic");
        epicJson.addProperty("description", "Test Epic Description");

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicGson.toJson(epicJson)))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        // Проверяем через manager
        assertEquals(1, manager.getListOfEpics().size());
        Epic epic = manager.getListOfEpics().get(0);
        assertEquals("Test Epic", epic.getName());
        assertTrue(epic.getSubtasks().isEmpty());
    }

    @Test
    void testEpicPersistence() throws IOException, InterruptedException, ManagerSaveException {
        // Создаем эпик через HTTP
        JsonObject epicJson = new JsonObject();
        epicJson.addProperty("name", "Persistent Epic");
        epicJson.addProperty("description", "Should persist after operations");

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicGson.toJson(epicJson)))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        // Перезагружаем manager из файла (имитируем перезапуск приложения)
        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(testFile.toString());

        // Проверяем, что эпик сохранился
        assertEquals(1, reloadedManager.getListOfEpics().size());
        Epic reloadedEpic = reloadedManager.getListOfEpics().get(0);
        assertEquals("Persistent Epic", reloadedEpic.getName());
    }

    @Test
    void testGetEpicWithSubtasks() throws IOException, InterruptedException, ManagerSaveException {
        // Создаем эпик
        Epic epic = new Epic("Parent Epic", "Description");
        manager.addEpic(epic);

        // Создаем подзадачи
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        // Получаем эпик по ID через HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic responseEpic = epicGson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getId(), responseEpic.getId());

        // Проверяем через manager
        Epic managerEpic = manager.getEpicById(epic.getId());
        assertEquals(2, managerEpic.getSubtasks().size());
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException, ManagerSaveException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.addEpic(epic);
        long epicId = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        // Проверяем через manager
        assertNull(manager.getEpicById(epicId));
        assertEquals(0, manager.getListOfEpics().size());
    }

    // ===== SUBTASK TESTS =====
    @Test
    void testCreateSubTaskWithEpic() throws IOException, InterruptedException, ManagerSaveException {
        // Сначала создаем эпик
        Epic epic = new Epic("Parent Epic", "Epic Description");
        manager.addEpic(epic);

        // Создаем подзадачу через HTTP
        JsonObject subTaskJson = new JsonObject();
        subTaskJson.addProperty("name", "Test SubTask");
        subTaskJson.addProperty("description", "Test SubTask Description");
        subTaskJson.addProperty("idMaster", epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskGson.toJson(subTaskJson)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        // Проверяем через manager
        assertEquals(1, manager.getListOfSubTasks().size());
        SubTask subTask = manager.getListOfSubTasks().get(0);
        assertEquals("Test SubTask", subTask.getName());
        assertEquals(epic.getId(), subTask.getMaster());

        // Проверяем, что подзадача добавилась в эпик
        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertEquals(1, updatedEpic.getSubtasks().size());
    }

    @Test
    void testSubTaskPersistence() throws IOException, InterruptedException, ManagerSaveException {
        // Создаем эпик
        Epic epic = new Epic("Parent Epic", "Description");
        manager.addEpic(epic);

        // Создаем подзадачу через HTTP
        JsonObject subTaskJson = new JsonObject();
        subTaskJson.addProperty("name", "Persistent SubTask");
        subTaskJson.addProperty("description", "Should persist in file");
        subTaskJson.addProperty("idMaster", String.valueOf(epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskGson.toJson(subTaskJson)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        // Перезагружаем manager из файла
        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(testFile.toString());

        // Проверяем, что подзадача сохранилась
        assertEquals(1, reloadedManager.getListOfSubTasks().size());
        SubTask reloadedSubTask = reloadedManager.getListOfSubTasks().get(0);
        assertEquals("Persistent SubTask", reloadedSubTask.getName());
        assertEquals(epic.getId(), reloadedSubTask.getMaster());
    }

    @Test
    void testDeleteSubTask() throws IOException, InterruptedException, ManagerSaveException {
        // Создаем эпик и подзадачу
        Epic epic = new Epic("Parent Epic", "Description");
        manager.addEpic(epic);

        SubTask subTask = new SubTask("Test SubTask", "Description", epic.getId());
        manager.addSubTask(subTask);
        long subTaskId = subTask.getId();

        // Удаляем подзадачу через HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks/" + subTaskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        // Проверяем удаление через manager
        assertNull(manager.getSubTaskById(subTaskId));
        assertEquals(0, manager.getListOfSubTasks().size());

        // Проверяем, что подзадача удалилась из эпика
        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtasks().isEmpty());
    }

    // ===== COMPLEX SCENARIOS =====
    @Test
    void testEpicSubTaskFullCycle() throws IOException, InterruptedException, ManagerSaveException {
        // 1. Создаем эпик
        JsonObject epicJson = new JsonObject();
        epicJson.addProperty("name", "Project Epic");
        epicJson.addProperty("description", "Main project epic");

        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicGson.toJson(epicJson)))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode());

        // Получаем ID созданного эпика через manager
        Epic createdEpic = manager.getListOfEpics().get(0);
        long epicId = createdEpic.getId();

        // 2. Создаем несколько подзадач
        for (int i = 1; i <= 3; i++) {
            JsonObject subTaskJson = new JsonObject();
            subTaskJson.addProperty("name", "SubTask " + i);
            subTaskJson.addProperty("description", "Description " + i);
            subTaskJson.addProperty("idMaster", epicId);

            HttpRequest subTaskRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/subtasks"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(subtaskGson.toJson(subTaskJson)))
                    .build();

            HttpResponse<String> subTaskResponse = client.send(subTaskRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, subTaskResponse.statusCode());
        }

        // 3. Проверяем через manager, что все создалось
        assertEquals(3, manager.getListOfSubTasks().size());
        Epic updatedEpic = manager.getEpicById(epicId);
        assertEquals(3, updatedEpic.getSubtasks().size());

        // 4. Перезагружаем из файла и проверяем сохранение
        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(testFile.toString());
        assertEquals(1, reloadedManager.getListOfEpics().size());
        assertEquals(3, reloadedManager.getListOfSubTasks().size());

        Epic reloadedEpic = reloadedManager.getListOfEpics().get(0);
        assertEquals(3, reloadedEpic.getSubtasks().size());
    }

    @Test
    void testHistoryPersistence() throws IOException, InterruptedException, ManagerSaveException {
        // Создаем задачи и эпик
        Task task = new Task("Test Task", "Description");
        manager.addTask(task);

        Epic epic = new Epic("Test Epic", "Description");
        manager.addEpic(epic);

        // Добавляем в историю через GET запросы
        client.send(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks/" + task.getId()))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics/" + epic.getId()))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());

        // Получаем историю
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());

        Task[] history = taskGson.fromJson(historyResponse.body(), Task[].class);
        assertEquals(2, history.length);

        // Проверяем историю через manager
        assertEquals(2, manager.getHistory().size());
    }

    @Test
    void testPrioritizedTasks() throws IOException, InterruptedException, ManagerSaveException {
        // Создаем задачи с разным временем начала
        Task earlyTask = new Task("Early Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 8, 0));
        Task lateTask = new Task("Late Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 10, 0));

        manager.addTask(earlyTask);
        manager.addTask(lateTask);

        // Получаем приоритетные задачи
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] prioritized = taskGson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritized.length);
        // Ранняя задача должна быть первой
        assertEquals("Early Task", prioritized[0].getName());

        // Проверяем через manager
        assertEquals(2, manager.getPrioritizedTasks().size());
    }

    // ===== ERROR HANDLING =====
    @Test
    void testCreateSubTaskWithInvalidEpic() throws IOException, InterruptedException {
        JsonObject subTaskJson = new JsonObject();
        subTaskJson.addProperty("name", "Test SubTask");
        subTaskJson.addProperty("description", "Description");
        subTaskJson.addProperty("idMaster", 999L); // Несуществующий эпик

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskGson.toJson(subTaskJson)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Должна быть ошибка, так как эпика не существует
        assertEquals(500, response.statusCode());

        // Проверяем, что подзадача не создалась в manager
        assertEquals(0, manager.getListOfSubTasks().size());
    }

    @Test
    void testGetNonExistentResource() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void testFileBackedTaskManagerIntegration() throws ManagerSaveException {
        // Комплексный тест интеграции FileBackedTaskManager с HTTP сервером

        // Создаем различные сущности
        Task task = new Task("Integration Task", "Description");
        manager.addTask(task);

        Epic epic = new Epic("Integration Epic", "Description");
        manager.addEpic(epic);

        SubTask subTask = new SubTask("Integration SubTask", "Description", epic.getId());
        manager.addSubTask(subTask);

        // Перезагружаем manager
        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(testFile.toString());

        // Проверяем, что все данные сохранились
        assertEquals(1, reloadedManager.getListOfTasks().size());
        assertEquals(1, reloadedManager.getListOfEpics().size());
        assertEquals(1, reloadedManager.getListOfSubTasks().size());

        // Проверяем связи
        Epic reloadedEpic = reloadedManager.getListOfEpics().get(0);
        assertEquals(1, reloadedEpic.getSubtasks().size());
        assertEquals(subTask.getId(), reloadedEpic.getSubtasks().get(0).getId());
    }
}