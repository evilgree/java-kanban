package test;

import manager.TaskManager;
import model.Task;
import model.Subtask;
import model.Epic;
import manager.Status;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected abstract T createTaskManager();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void testCreateAndGetTask() {
        Task task = new Task("Test task", "Description", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofMinutes(30));
        taskManager.createTask(task);

        Task retrieved = taskManager.getTaskById(task.getId());
        assertNotNull(retrieved);
        assertEquals(task, retrieved);
    }

    @Test
    void testCreateAndGetEpic() {
        Epic epic = new Epic("Epic", "Epic description");
        taskManager.createEpic(epic);
        Epic retrieved = taskManager.getEpicById(epic.getId());
        assertNotNull(retrieved);
        assertEquals(epic, retrieved);
    }

    @Test
    void testCreateAndGetSubtask() {
        Epic epic = new Epic("Epic", "Epic description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId(), Status.NEW);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofMinutes(15));
        taskManager.createSubtask(subtask);

        Subtask retrieved = taskManager.getSubtaskById(subtask.getId());
        assertNotNull(retrieved);
        assertEquals(subtask, retrieved);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().contains(subtask.getId()));
    }


    @Test
    void testEpicStatus_allNew() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask s1 = new Subtask("Sub1", "d", epic.getId(), Status.NEW);
        Subtask s2 = new Subtask("Sub2", "d", epic.getId(), Status.NEW);
        taskManager.createSubtask(s1);
        taskManager.createSubtask(s2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.NEW, updatedEpic.getStatus());
    }

    @Test
    void testEpicStatus_allDone() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask s1 = new Subtask("Sub1", "d", epic.getId(), Status.DONE);
        Subtask s2 = new Subtask("Sub2", "d", epic.getId(), Status.DONE);
        taskManager.createSubtask(s1);
        taskManager.createSubtask(s2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, updatedEpic.getStatus());
    }

    @Test
    void testEpicStatus_newAndDone() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask s1 = new Subtask("Sub1", "d", epic.getId(), Status.NEW);
        Subtask s2 = new Subtask("Sub2", "d", epic.getId(), Status.DONE);
        taskManager.createSubtask(s1);
        taskManager.createSubtask(s2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void testEpicStatus_inProgress() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask s1 = new Subtask("Sub1", "d", epic.getId(), Status.IN_PROGRESS);
        taskManager.createSubtask(s1);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }


    @Test
    void testTaskTimeIntersectionThrows() {
        Task task1 = new Task("Task 1", "desc", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "desc", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30)); // пересекается с task1
        task2.setDuration(Duration.ofHours(1));

        assertThrows(RuntimeException.class, () -> taskManager.createTask(task2),
                "Создание пересекающейся задачи должно выбрасывать исключение");
    }

    @Test
    void testTaskTimeNoIntersection() {
        Task task1 = new Task("Task 1", "desc", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "desc", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0)); // не пересекается
        task2.setDuration(Duration.ofHours(1));

        assertDoesNotThrow(() -> taskManager.createTask(task2));
    }

    @Test
    void testHistoryEmptyInitially() {
        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История изначально пустая");
    }

    @Test
    void testHistoryNoDuplicatesAndOrder() {
        Task task1 = new Task("Task 1", "desc", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "desc", Status.NEW);
        taskManager.createTask(task2);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(List.of(task2, task1), history, "Последний просмотренный должен быть в конце");
    }

    @Test
    void testRemoveFromHistory() {
        Task task1 = new Task("Task 1", "desc", Status.NEW);
        Task task2 = new Task("Task 2", "desc", Status.NEW);
        Task task3 = new Task("Task 3", "desc", Status.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());

        taskManager.getHistory().remove(task2);

        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(task2));
        assertEquals(2, history.size());
    }
}