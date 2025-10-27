package main.java.server.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import main.java.exception.ManagerSaveException;
import main.java.server.handler.adapter.DurationTypeAdapter;
import main.java.server.handler.adapter.LocalDateTimeTypeAdapter;
import main.java.server.handler.adapter.SubTaskTypeAdapter;
import main.java.task.SubTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(SubTask.class, new SubTaskTypeAdapter())
            .setPrettyPrinting()
            .create();

    @Override
    public void handle(HttpExchange exchange) {
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

    private void handleGetSubtasks(HttpExchange exchange) throws ManagerSaveException {
        List<SubTask> tasks = manager.getListOfSubTasks();
        sendText(exchange,
                gson.toJson(tasks, SUBTASK_LIST_TYPE),
                200);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws ManagerSaveException {
        SubTask subtask = manager.getSubTaskById(getId(exchange));
        sendText(exchange,
                gson.toJson(subtask, SUBTASK_TYPE),
                200);
    }

    private void handlePostSubtask(HttpExchange exchange) throws ManagerSaveException {
        String body = getRequestBody(exchange);
        SubTask subtask = gson.fromJson(body, SubTask.class);

//        if (JsonParser.parseString(getRequestBody(exchange)).getAsJsonObject().has("id")) {
//            manager.updateSubTask(subtask);
//        } else {
        manager.addSubTask(subtask);
//        }
        sendText(exchange, "", 201);
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws ManagerSaveException {
        manager.removeSubTaskById(getId(exchange));
        sendText(exchange, "", 200);
    }

}
