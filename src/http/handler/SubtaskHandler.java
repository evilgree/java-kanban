package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.*;
import model.*;
import http.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            String jsonResponse = gson.toJson(subtasks);
            sendText(exchange, jsonResponse, 200);
        } else {
            String id = path.substring(path.lastIndexOf("/") + 1);
            Subtask subtask = taskManager.getSubtaskById(Integer.parseInt(id));
            if (subtask != null) {
                String jsonResponse = gson.toJson(subtask);
                sendText(exchange, jsonResponse, 200);
            } else {
                sendNotFound(exchange);
            }
        }
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        taskManager.createSubtask(subtask);
        sendText(exchange, "Subtask created", 201);
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery(); // например "id=4"
        if (query == null || !query.startsWith("id=")) {
            sendText(exchange, "Missing or invalid id parameter", 400);
            return;
        }
        String idStr = query.substring(3);
        try {
            int id = Integer.parseInt(idStr);
            taskManager.deleteSubtask(id);
            sendText(exchange, "Subtask deleted", 200);
        } catch (NumberFormatException e) {
            sendText(exchange, "Invalid id parameter", 400);
        }
    }
}