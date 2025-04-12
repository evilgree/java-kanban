package test;
import manager.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void testInMemoryTaskManagerAddsAndFindsTasks() {
        Task task = new Task("Новая Задача", "Описание новой задачи", Status.NEW);

        taskManager.createTask(task);

        Task foundTask = taskManager.getTaskById(task.getId());

        assertNotNull(foundTask, "Задача должна быть найдена по ID.");
        assertEquals(task, foundTask, "Найдённая задача должна совпадать с созданной задачей.");
    }

    @Test
    void testNoConflictBetweenTasksWithSameGeneratedIds() {
        Task task1 = new Task("Задача A", "Описание A", Status.NEW);
        Task task2 = new Task("Задача B", "Описание B", Status.NEW);

        int id1 = taskManager.createTask(task1).getId();
        int id2 = taskManager.createTask(task2).getId();

        assertNotEquals(id1, id2, "Задачи должны иметь разные ID.");
    }

    @Test
    void testImmutableTaskWhenAddedToManager() {
        Task originalTask = new Task("Исходная Задача", "Исходное Описание", Status.NEW);

        int id = taskManager.createTask(originalTask).getId();



        String newTitle = "Изменённый Заголовок";
        Task modifiedTask = new Task(newTitle, originalTask.getDescription(), originalTask.getStatus());

        Task retrievedTask = taskManager.getTaskById(id);


        assertEquals(originalTask.getTitle(), retrievedTask.getTitle(),
                "Название извлечённой задачи должно оставаться прежним.");


        assertNotEquals(modifiedTask.getTitle(), retrievedTask.getTitle(),
                "Название извлечённой задачи не должно изменяться после изменения оригинала.");
    }

    @Test
    void testHistoryManagerRecordsTaskView() {
        Task task = new Task("Тестовая задача", "Описание задачи", Status.NEW);
        taskManager.createTask(task);


        Task retrievedTask = taskManager.getTaskById(task.getId());


        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(retrievedTask, history.get(0), "Задача в истории должна соответствовать полученной задаче.");
    }
}