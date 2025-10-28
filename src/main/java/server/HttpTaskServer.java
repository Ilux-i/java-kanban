package main.java.server;

import com.sun.net.httpserver.HttpServer;
import main.java.exception.ManagerSaveException;
import main.java.manager.FileBackedTaskManager;
import main.java.manager.TaskManager;
import main.java.server.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    public static TaskManager manager;

    private static final int PORT = 8080;
    private static HttpServer server;

    public static void main(String[] args) {
        start("data.csv");
    }

    public static void start(String file) {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/tasks", new TaskHandler());
            server.createContext("/epics", new EpicHandler());
            server.createContext("/subtasks", new SubtaskHandler());
            server.createContext("/history", new HistoryHandler());
            server.createContext("/prioritized", new PrioritizedHandler());
            server.start();
            manager = FileBackedTaskManager.loadFromFile(file);
            BaseHttpHandler.setManager(manager);
            System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        } catch (IOException | ManagerSaveException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        server.stop(0);
    }
}