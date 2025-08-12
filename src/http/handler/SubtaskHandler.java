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
        if (subtask == null) {
            sendText(exchange, "Invalid subtask data", 400);
            return;
        }
        if (subtask.getId() == 0) {
            taskManager.createSubtask(subtask);
            sendText(exchange, "Subtask created", 201);
        } else {
            if (taskManager.getSubtaskById(subtask.getId()) != null) {
                taskManager.updateSubtask(subtask);
                sendText(exchange, "Subtask updated", 201);
            } else {
                sendNotFound(exchange);
            }
        }
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length == 2) {
            taskManager.deleteAllSubtasks();
            sendText(exchange, "All subtasks deleted", 200);
        } else if (parts.length == 3) {
            try {
                int id = Integer.parseInt(parts[2]);
                if (taskManager.getSubtaskById(id) != null) {
                    taskManager.deleteSubtask(id);
                    sendText(exchange, "Subtask deleted", 200);
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