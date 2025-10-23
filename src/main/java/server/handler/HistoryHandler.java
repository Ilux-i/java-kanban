package main.java.server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

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
        Gson gson = new Gson();
        sendText(exchange,
                gson.toJson(manager.getHistory()),
                200);
    }
}
