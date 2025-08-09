package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import http.BaseHttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Received request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query == null) {
                        List<Epic> epics = taskManager.getAllEpics();
                        String response = gson.toJson(epics);
                        sendText(exchange, response, 200);
                    }
                    break;

                case "POST":
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Epic epic = gson.fromJson(body, Epic.class);
                    taskManager.createEpic(epic);
                    sendText(exchange, "Epic created", 201);
                    break;

                case "DELETE":
                    if (query == null || !query.startsWith("id=")) {
                        sendText(exchange, "Bad request", 400);
                        break;
                    }
                    int id = Integer.parseInt(query.substring(3));
                    if (taskManager.getEpicById(id) != null) {
                        taskManager.deleteEpicById(id);
                        sendText(exchange, "Epic deleted", 200);
                    } else {
                        sendText(exchange, "Epic not found", 404);
                    }
                    break;

                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendText(exchange, "Internal server error", 500);
        }
        System.out.println("Completed request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
    }
}