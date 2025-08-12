package http;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import manager.*;
import model.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.time.LocalDateTime;
import http.adapters.DurationAdapter;
import http.adapters.LocalDateTimeAdapter;


public class HttpTaskManagerSubtasksTest {

    private static TaskManager manager;
    private static HttpTaskServer taskServer;
    private HttpClient client;
    private Gson gson;

    @BeforeAll
    public static void startServer() throws IOException {
        HistoryManager historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterAll
    public static void stopServer() {
        taskServer.stop();
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description of epic");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Description of subtask", epic.getId(), Status.NEW);
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getAllSubtasks();
        assertNotNull(subtasksFromManager);
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Subtask 1", subtasksFromManager.get(0).getTitle());
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description of epic");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), Status.NEW);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), Status.NEW);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {}.getType());
        assertEquals(2, subtasks.size());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description of epic");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask to delete", "Description", epic.getId(), Status.NEW);
        manager.createSubtask(subtask);

        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}