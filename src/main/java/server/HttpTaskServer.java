package main.java.server;

import com.sun.net.httpserver.HttpServer;
import main.java.server.handler.BaseHttpHandler;
import main.java.server.handler.TaskHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static HttpServer server;

    public static void main(String[] args) {
        start();

        close();
    }

    private static void start(){
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/tasks", new TaskHandler());
            server.createContext("/epics", new BaseHttpHandler());
            server.createContext("/subtasks", new BaseHttpHandler());
            server.createContext("/history", new TaskHandler());
            server.createContext("/prioritized", new BaseHttpHandler());
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