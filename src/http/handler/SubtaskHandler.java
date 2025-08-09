package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.*;
import model.*;
import http.BaseHttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query == null) {
                        List<Subtask> subtasks = taskManager.getAllSubtasks();
                        String response = gson.toJson(subtasks);
                        sendText(exchange, response, 200);
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        Subtask subtask = taskManager.getSubtaskById(id);
                        if (subtask != null) {
                            String response = gson.toJson(subtask);
                            sendText(exchange, response, 200);
                        } else {
                            sendText(exchange, "Subtask not found", 404);
                        }
                    } else {
                        sendText(exchange, "Bad request", 400);
                    }
                    break;

                case "POST":
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Subtask subtask = gson.fromJson(body, Subtask.class);

                    if (subtask == null) {
                        sendText(exchange, "Invalid subtask data", 400);
                        break;
                    }

                    if (taskManager.hasIntersection(subtask)) {
                        sendText(exchange, "Subtask time intersects with existing task", 406);
                        break;
                    }

                    if (subtask.getId() == 0) {
                        taskManager.createSubtask(subtask);
                        sendText(exchange, "Subtask created", 201);
                    } else {
                        if (taskManager.getSubtaskById(subtask.getId()) != null) {
                            taskManager.updateSubtask(subtask);
                            sendText(exchange, "Subtask updated", 201);
                        } else {
                            sendText(exchange, "Subtask not found for update", 404);
                        }
                    }
                    break;

                case "DELETE":
                    if (query == null) {
                        taskManager.deleteAllSubtasks();
                        sendText(exchange, "All subtasks deleted", 200);
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        if (taskManager.getSubtaskById(id) != null) {
                            taskManager.deleteSubtaskById(id);
                            sendText(exchange, "Subtask deleted", 200);
                        } else {
                            sendText(exchange, "Subtask not found", 404);
                        }
                    } else {
                        sendText(exchange, "Bad request", 400);
                    }
                    break;

                default:
                    sendText(exchange, "Method not allowed", 405);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendText(exchange, "Internal server error", 500);
        }
    }
}