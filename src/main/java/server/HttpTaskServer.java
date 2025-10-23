package main.java.server;

import com.sun.net.httpserver.HttpServer;
import main.java.server.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static HttpServer server;

    public static void main(String[] args) {
        start();
    }

    private static void start(){
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/tasks", new TaskHandler());
            server.createContext("/epics", new EpicHandler());
            server.createContext("/subtasks", new SubtaskHandler());
            server.createContext("/history", new HistoryHandler());
            server.createContext("/prioritized", new PrioritizedHandler());
            server.start();
            System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void close(){
        server.stop(0);
    }
}