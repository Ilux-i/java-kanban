package main.java.server.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EpicHandler extends  BaseHttpHandler{

    @Override
    public void handle(HttpExchange exchange){
        try {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    if (exchange.getRequestURI().getPath().split("/").length > 3) {
                        handleGetEpicSubtasks(exchange);
                    } else if (exchange.getRequestURI().getPath().split("/").length > 2) {
                        handleGetEpicById(exchange);
                    } else {
                        handleGetEpics(exchange);
                    }
                    break;
                case "POST":
                    handlePostEpic(exchange);
                    break;
                case "DELETE":
                    handleDeleteEpic(exchange);
                    break;
                default:
                    throw new Exception("Unsupported HTTP method: " + method);
            }
        } catch (Exception e) {
            handleException(e, exchange);
        }
    }

    private void handleGetEpicById(HttpExchange exchange) {
        Gson gson = new Gson();
        List<Epic> epics = manager.getListOfEpics();
        sendText(exchange,
                gson.toJson(epics),
                200);
    }

    private void handleGetEpics(HttpExchange exchange) {
        Gson gson = new Gson();
        Epic epic = manager.getEpicById(getId(exchange));
        sendText(exchange,
                gson.toJson(epic),
                200);
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) {
        Gson gson = new Gson();
        JsonObject data = JsonParser.parseString(getRequestBody(exchange)).getAsJsonObject();
        Epic epic = manager.getEpicById(getId(exchange));
        sendText(exchange,
                gson.toJson(epic.getSubtasks()),
                200);
    }

    private void handlePostEpic(HttpExchange exchange) {
        Gson gson = new Gson();
        JsonObject data = JsonParser.parseString(getRequestBody(exchange)).getAsJsonObject();
        String name = data.get("name").getAsString();
        String description = data.get("description").getAsString();
        Epic epic = new Epic(name, description);

        if(data.has("duration")){
            Duration duration = Duration.parse(data.get("duration").toString());
            LocalDateTime startTime = LocalDateTime.parse(data.get("startTime").getAsString());
            epic = new Epic(name, description, duration, startTime);
        }

        if(data.has("id")) {
            epic.setId(data.get("id").getAsLong());
            epic.setSubtasks(StreamSupport
                    .stream(
                            data.get("subtasks")
                                    .getAsJsonArray()
                                    .spliterator(), false
                    )
                    .map((JsonElement t) -> gson.fromJson(t, SubTask.class))
                    .collect(Collectors.toCollection(ArrayList::new)));
            manager.updateEpic(epic);
        } else {
            manager.addEpic(epic);
        }
        sendText(exchange, "", 201);
    }

    private void handleDeleteEpic(HttpExchange exchange) {
        manager.removeEpicById(getId(exchange));
        sendText(exchange, "", 200);
    }
}
