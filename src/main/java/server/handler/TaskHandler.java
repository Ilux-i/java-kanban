package main.java.server.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.task.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                if (exchange.getRequestURI().getPath().split("/").length > 2) {
                    handleGetTaskById(exchange);
                } else {
                    handleGetTasks(exchange);
                }
                break;
            case "POST":
                handlePostTask(exchange);
                break;
            case "DELETE":
                handleDeleteTask(exchange);
                break;
        }
    }

    private void handleGetTasks(HttpExchange exchange){
        Gson gson = new Gson();
        List<Task> tasks = manager.getListOfTasks();
        sendText(exchange, gson.toJson(tasks), 200);
    }

    private void handleGetTaskById(HttpExchange exchange){
        Gson gson = new Gson();
        Task task = manager.getTaskById(getId(exchange));
        sendText(exchange, gson.toJson(task), 200);
    }

    private void handlePostTask(HttpExchange exchange){
        JsonObject data = JsonParser.parseString(getRequestBody(exchange)).getAsJsonObject();
        String name = data.get("name").getAsString();
        String description = data.get("description").getAsString();
        Task task = new Task(name, description);
        try{
            Duration duration = Duration.parse(data.get("duration").toString());
            LocalDateTime startTime = LocalDateTime.parse(data.get("startTime").getAsString());
            task = new Task(name, description, duration, startTime);
        } catch (Exception ignored) {}
        if (exchange.getRequestURI().getPath().split("/").length > 2) {
            task.setId(getId(exchange));
            manager.updateTask(task);
        } else {
            manager.addTask(task);
            sendText(exchange, "", 201);
        }
    }

    private void handleDeleteTask(HttpExchange exchange){
        manager.removeTaskById(getId(exchange));
        sendText(exchange, "", 200);
    }


}
