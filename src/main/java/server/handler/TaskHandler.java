package main.java.server.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import main.java.exception.ManagerSaveException;
import main.java.server.handler.adapter.DurationTypeAdapter;
import main.java.server.handler.adapter.LocalDateTimeTypeAdapter;
import main.java.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            switch (Method.valueOf(method)) {
                case GET:
                    if (exchange.getRequestURI().getPath().split("/").length > 2) {
                        handleGetTaskById(exchange);
                    } else {
                        handleGetTasks(exchange);
                    }
                    break;
                case POST:
                    handlePostTask(exchange);
                    break;
                case DELETE:
                    handleDeleteTask(exchange);
                    break;
                default:
                    throw new Exception("Unsupported HTTP method: " + method);
            }
        } catch (Exception e) {
            handleException(e, exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws ManagerSaveException {
        List<Task> tasks = manager.getListOfTasks();
        sendText(exchange,
                gson.toJson(tasks, TASK_LIST_TYPE),
                200);
    }

    private void handleGetTaskById(HttpExchange exchange) throws ManagerSaveException {
        Task task = manager.getTaskById(getId(exchange));
        if (task == null) {
            throw new ManagerSaveException("Задача не найдена");
        }
        sendText(exchange,
                gson.toJson(task, TASK_TYPE),
                200);
    }

    private void handlePostTask(HttpExchange exchange) throws ManagerSaveException {
        JsonObject data = JsonParser.parseString(getRequestBody(exchange)).getAsJsonObject();
        String name = data.get("name").getAsString();
        String description = data.get("description").getAsString();
        Task task = new Task(name, description);
        if (data.has("duration")) {
            Duration duration = gson.fromJson(data.get("duration"), Duration.class);
            LocalDateTime startTime = gson.fromJson(data.get("startTime"), LocalDateTime.class);
            task = new Task(name, description, duration, startTime);
        }
        if (data.has("id")) {
            task.setId(data.get("id").getAsLong());
            manager.updateTask(task);
        } else {
            manager.addTask(task);
        }
        sendText(exchange,
                "",
                201);
    }

    private void handleDeleteTask(HttpExchange exchange) throws ManagerSaveException {
        manager.removeTaskById(getId(exchange));
        sendText(exchange,
                "",
                200);
    }


}
