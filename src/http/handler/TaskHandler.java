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
        if (task == null) {
            sendText(exchange, "Invalid task data", 400);
            return;
        }
        if (task.getId() == 0) {
            taskManager.createTask(task);
            sendText(exchange, "Task created", 201);
        } else {
            if (taskManager.getTaskById(task.getId()) != null) {
                taskManager.updateTask(task);
                sendText(exchange, "Task updated", 201);
            } else {
                sendNotFound(exchange);
            }
        }
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length == 2) {
            taskManager.deleteAllTasks();
            sendText(exchange, "All tasks deleted", 200);
        } else if (parts.length == 3) {
            try {
                int id = Integer.parseInt(parts[2]);
                if (taskManager.getTaskById(id) != null) {
                    taskManager.deleteTask(id);
                    sendText(exchange, "Task deleted", 200);
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