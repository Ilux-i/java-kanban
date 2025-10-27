package main.java.server.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import main.java.server.handler.adapter.DurationTypeAdapter;
import main.java.server.handler.adapter.LocalDateTimeTypeAdapter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class HistoryHandler extends BaseHttpHandler {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                handleGetHistory(exchange);
            } else {
                throw new Exception("Unsupported HTTP method: " + method);
            }
        } catch (Exception e) {
            handleException(e, exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) {
        sendText(exchange,
                gson.toJson(manager.getHistory(), TASK_LIST_TYPE),
                200);
    }
}
