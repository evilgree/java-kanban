package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;
import http.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            String jsonResponse = gson.toJson(tasks);
            sendText(exchange, jsonResponse, 200);
        } else {
            String id = path.substring(path.lastIndexOf("/") + 1);
            Task task = taskManager.getTaskById(Integer.parseInt(id));
            if (task != null) {
                String jsonResponse = gson.toJson(task);
                sendText(exchange, jsonResponse, 200);
            } else {
                sendNotFound(exchange);
            }
        }
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Task task = gson.fromJson(body, Task.class);
        taskManager.createTask(task);
        sendText(exchange, "Task created", 201);
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
            taskManager.deleteTask(id);
            sendText(exchange, "Task deleted", 200);
        } catch (NumberFormatException e) {
            sendText(exchange, "Invalid id parameter", 400);
        }
    }
}