package main.java.server.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.manager.FileBackedTaskManager;
import main.java.manager.Managers;
import main.java.server.HttpTaskServer;
import main.java.server.handler.adapter.DurationTypeAdapter;
import main.java.server.handler.adapter.LocalDateTimeTypeAdapter;
import main.java.task.Epic;
import main.java.task.SubTask;
import main.java.task.Task;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseHttpHandler implements HttpHandler {

    public static FileBackedTaskManager manager;
    protected static final Type TASK_LIST_TYPE = new TypeToken<List<Task>>() {
    }.getType();
    protected static final Type EPIC_LIST_TYPE = new TypeToken<List<Epic>>() {
    }.getType();
    protected static final Type SUBTASK_LIST_TYPE = new TypeToken<List<SubTask>>() {
    }.getType();
    protected static final Type TASK_TYPE = new TypeToken<Task>() {
    }.getType();
    protected static final Type EPIC_TYPE = new TypeToken<Epic>() {
    }.getType();
    protected static final Type SUBTASK_TYPE = new TypeToken<SubTask>() {
    }.getType();

    protected void sendText(HttpExchange h, String text, int code) {
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        try (OutputStream os = h.getResponseBody()) {
            h.sendResponseHeaders(code, 0);
            os.write(text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при добавлении текста в ответ в sendText");
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }

    protected long getId(HttpExchange exchange) {
        return Long.parseLong(exchange.
                getRequestURI().
                getPath().
                split("/")[2]);
    }

    protected String getRequestBody(HttpExchange exchange) {
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        } catch (IOException e) {
            try {
                throw new RuntimeException("Ошибка при преобразовании тела запроса в json");
            } catch (Exception e1) {
                handleException(e1, exchange);
            }
        }
        return jsonBuilder.toString();
    }

    protected void handleException(Exception exception, HttpExchange exchange) {
        int code = 500;
        switch (exception.getMessage()) {
            case "Задача не найдена":
                code = 404;
                break;
        }
        sendText(exchange,
                exception.getMessage(),
                code);
    }

}
