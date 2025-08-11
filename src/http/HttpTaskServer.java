package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

import http.Adapters.DurationAdapter;
import http.Adapters.LocalDateTimeAdapter;
import manager.*;
import http.handler.TaskHandler;
import http.handler.SubtaskHandler;
import http.handler.EpicHandler;
import http.handler.HistoryHandler;
import http.handler.*;

public class HttpTaskServer {
    private final TaskManager taskManager;
    private final Gson gson;
    private HttpServer server;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(java.time.Duration.class, new DurationAdapter())
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        server = HttpServer.create(new InetSocketAddress(8080), 0);
        registerContexts();
    }

    protected void registerContexts() {
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        server.start();
        System.out.println("Server started on port 8080");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.start();
        httpTaskServer.stop();
    }
}