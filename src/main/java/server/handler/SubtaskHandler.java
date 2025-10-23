package main.java.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import main.java.task.SubTask;
import main.java.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler{

    @Override
    public void handle(HttpExchange exchange){
        try {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    if (exchange.getRequestURI().getPath().split("/").length > 2) {
                        handleGetSubtaskById(exchange);
                    } else {
                        handleGetSubtasks(exchange);
                    }
                    break;
                case "POST":
                    handlePostSubtask(exchange);
                    break;
                case "DELETE":
                    handleDeleteSubtask(exchange);
                    break;
                default:
                    throw new Exception("Unsupported HTTP method: " + method);
            }
        } catch (Exception e) {
            handleException(e, exchange);
        }
    }

    private void handleGetSubtasks(HttpExchange exchange){
        Gson gson = new Gson();
        List<SubTask> tasks = manager.getListOfSubTasks();
        sendText(exchange,
                gson.toJson(tasks),
                200);
    }

    private void handleGetSubtaskById(HttpExchange exchange){
        Gson gson = new Gson();
        SubTask subtask = manager.getSubTaskById(getId(exchange));
        sendText(exchange,
                gson.toJson(subtask),
                200);
    }

    private void handlePostSubtask(HttpExchange exchange){
        JsonObject data = JsonParser.parseString(getRequestBody(exchange)).getAsJsonObject();
        String name = data.get("name").getAsString();
        String description = data.get("description").getAsString();
        int idMaster = data.get("idMaster").getAsInt();
        SubTask subtask = new SubTask(name, description, idMaster);
        if(data.has("duration")) {
            Duration duration = Duration.parse(data.get("duration").toString());
            LocalDateTime startTime = LocalDateTime.parse(data.get("startTime").getAsString());
            subtask = new SubTask(name, description, idMaster, duration, startTime);
        }
        if (data.has("id")) {
            subtask.setId(data.get("id").getAsLong());
            manager.updateTask(subtask);
        } else {
            manager.addTask(subtask);
        }
        sendText(exchange, "", 201);
    }

    private void handleDeleteSubtask(HttpExchange exchange){
        manager.removeSubTaskById(getId(exchange));
        sendText(exchange, "", 200);
    }

}
