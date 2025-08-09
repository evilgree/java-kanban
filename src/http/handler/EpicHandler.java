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
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query == null) {
                        List<Epic> epics = taskManager.getAllEpics();
                        String response = gson.toJson(epics);
                        sendText(exchange, response, 200);
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        Epic epic = taskManager.getEpicById(id);
                        if (epic != null) {
                            String response = gson.toJson(epic);
                            sendText(exchange, response, 200);
                        } else {
                            sendText(exchange, "Epic not found", 404);
                        }
                    } else {
                        sendText(exchange, "Bad request", 400);
                    }
                    break;

                case "POST":
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Epic epic = gson.fromJson(body, Epic.class);

                    if (epic == null) {
                        sendText(exchange, "Invalid epic data", 400);
                        break;
                    }

                    if (taskManager.hasIntersection(epic)) {
                        sendText(exchange, "Epic time intersects with existing task", 406);
                        break;
                    }

                    if (epic.getId() == 0) {
                        taskManager.createEpic(epic);
                        sendText(exchange, "Epic created", 201);
                    } else {
                        if (taskManager.getEpicById(epic.getId()) != null) {
                            taskManager.updateEpic(epic);
                            sendText(exchange, "Epic updated", 201);
                        } else {
                            sendText(exchange, "Epic not found for update", 404);
                        }
                    }
                    break;

                case "DELETE":
                    if (query == null) {
                        taskManager.deleteAllEpics();
                        sendText(exchange, "All epics deleted", 200);
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        if (taskManager.getEpicById(id) != null) {
                            taskManager.deleteEpicById(id);
                            sendText(exchange, "Epic deleted", 200);
                        } else {
                            sendText(exchange, "Epic not found", 404);
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