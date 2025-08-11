package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import http.BaseHttpHandler;

import java.io.IOException;
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
            switch (exchange.getRequestMethod()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendText(exchange, "Method not allowed", 405);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendText(exchange, "Internal server error", 500);
        }
        System.out.println("Completed request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
    }

    protected void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 2) {
            List<Epic> epics = taskManager.getAllEpics();
            String response = gson.toJson(epics);
            sendText(exchange, response, 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                Epic epic = taskManager.getEpicById(id);
                if (epic != null) {
                    sendText(exchange, gson.toJson(epic), 200);
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendText(exchange, "Invalid id format", 400);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);
        if (epic == null) {
            sendText(exchange, "Invalid epic data", 400);
            return;
        }
        if (epic.getId() == 0) {
            taskManager.createEpic(epic);
            sendText(exchange, "Epic created", 201);
        } else {
            if (taskManager.getEpicById(epic.getId()) != null) {
                taskManager.updateEpic(epic);
                sendText(exchange, "Epic updated", 201);
            } else {
                sendNotFound(exchange);
            }
        }
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 2) {
            taskManager.deleteAllEpics();
            sendText(exchange, "All epics deleted", 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                if (taskManager.getEpicById(id) != null) {
                    taskManager.deleteEpic(id);
                    sendText(exchange, "Epic deleted", 200);
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendText(exchange, "Invalid id format", 400);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}