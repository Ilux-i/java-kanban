package main.java.server.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import main.java.exception.ManagerSaveException;
import main.java.server.handler.adapter.DurationTypeAdapter;
import main.java.server.handler.adapter.EpicTypeAdapter;
import main.java.server.handler.adapter.LocalDateTimeTypeAdapter;
import main.java.task.Epic;

import java.time.Duration;

import java.time.LocalDateTime;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(EPIC_TYPE, new EpicTypeAdapter())
            .setPrettyPrinting()
            .create();

    @Override
    public void handle(HttpExchange exchange) {
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

    private void handleGetEpicById(HttpExchange exchange) throws ManagerSaveException {
        Epic epics = manager.getEpicById(getId(exchange));
        sendText(exchange,
                gson.toJson(epics, EPIC_TYPE),
                200);
    }

    private void handleGetEpics(HttpExchange exchange) throws ManagerSaveException {
        List<Epic> epic = manager.getListOfEpics();
        sendText(exchange,
                gson.toJson(epic, EPIC_LIST_TYPE),
                200);
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws ManagerSaveException {
        Epic epic = manager.getEpicById(getId(exchange));
        sendText(exchange,
                gson.toJson(epic.getSubtasks(), SUBTASK_LIST_TYPE),
                200);
    }

    private void handlePostEpic(HttpExchange exchange) throws ManagerSaveException {
        String body = getRequestBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);

//        if(JsonParser.parseString(getRequestBody(exchange)).getAsJsonObject().has("id")) {
//            manager.updateEpic(epic);
//        } else {
        manager.addEpic(epic);
        sendText(exchange, "", 201);
    }

    private void handleDeleteEpic(HttpExchange exchange) throws ManagerSaveException {
        manager.removeEpicById(getId(exchange));
        sendText(exchange, "", 200);
    }
}
