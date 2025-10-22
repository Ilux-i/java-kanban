package main.java.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.manager.FileBackedTaskManager;
import main.java.manager.Managers;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler implements HttpHandler {

    protected static FileBackedTaskManager manager = Managers.getDefaultFileBackedTaskManager();

    protected void sendText(HttpExchange h, String text, int code){
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        try(OutputStream os = h.getResponseBody()) {
            h.sendResponseHeaders(code, 0);
            os.write(text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при добавлении текста в ответ в sendText");
        }
    }

    protected String getRequestBody(HttpExchange exchange) {
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }catch (IOException e) {
            throw new RuntimeException("Ошибка при преобразовании тела запроса в json");
        }
        return jsonBuilder.toString();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        h.sendResponseHeaders(404, 0);
        h.close();
    }

    protected void sendHasOverlaps(HttpExchange h) throws IOException {
        h.sendResponseHeaders(406, 0);
        h.close();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }

    protected long getId(HttpExchange exchange){
        return Long.parseLong(exchange.
                getRequestURI().
                getPath().
                split("/")[2]);
    }

}
