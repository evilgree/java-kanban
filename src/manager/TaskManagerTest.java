package manager;

import manager.InMemoryTaskManager;
import manager.InMemoryHistoryManager;
import model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager(new InMemoryHistoryManager());
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testTaskEqualityById() {
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(1, "Task 2", "Description 2", Status.NEW);
        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны.");
    }

    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description 1", Status.NEW, 2);
        Subtask subtask2 = new Subtask(1, "Subtask 2", "Description 2", Status.NEW, 3);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны.");
    }

    @Test
    void testEpicCannotBeItsOwnSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Epic description");
        Subtask subtask = new Subtask(1, "Subtask 1", "Description", Status.NEW, epic.getId());
        assertFalse(taskManager.addSubtask(subtask), "Эпик не может быть добавлен как своя собственная подзадача.");
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Subtask subtask = new Subtask(1, "Subtask 1", "Description", Status.NEW, 1);
        assertFalse(taskManager.addSubtask(subtask), "Подзадача не может быть своим собственным эпиком.");
    }

    @Test
    void testManagersInitialization() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Утилитарный класс менеджеров должен возвращать ненулевой экземпляр.");
    }

    @Test
    void testInMemoryTaskManagerAddAndFindTasks() {
        Task task = new Task(1, "Task 1", "Description 1", Status.NEW);
        taskManager.addNewTask(task);

        Task foundTask = taskManager.getTask(1);
        assertNotNull(foundTask, "Задача должна быть найдена по ID.");
        assertEquals(task, foundTask, "Найдённая задача должна совпадать с добавленной задачей.");

        List<Task> tasks = taskManager.getTasks();
        assertEquals(1, tasks.size(), "В менеджере должна быть одна задача.");
    }

    @Test
    void testNoConflictWithGeneratedIds() {
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description 2", Status.NEW);

        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "Задачи с разными ID не должны конфликтовать.");

        List<Task> tasks = taskManager.getTasks();
        assertEquals(2, tasks.size(), "В менеджере должно быть две задачи.");
    }

    @Test
    void testImmutabilityOfTaskWhenAddedToManager() {
        Task originalTask = new Task(3, "Original Task", "Original Description", Status.NEW);

        // Add the original task to the manager
        taskManager.addNewTask(originalTask);

        // Retrieve the saved version and check if it matches the original
        Task savedTask = taskManager.getTask(originalTask.getId());

        assertEquals(originalTask.getTitle(), savedTask.getTitle(),
                "Заголовок сохранённой задачи должен совпадать с оригиналом.");

        assertEquals(originalTask.getDescription(), savedTask.getDescription(),
                "Описание сохранённой задачи должно совпадать с оригиналом.");

        assertEquals(originalTask.getStatus(), savedTask.getStatus(),
                "Статус сохранённой задачи должен совпадать с оригиналом.");
    }

    @Test
    void testHistoryManagement() {
        Task task = new Task(4, "History Test Task", "History Test Description", Status.NEW);

        // Add to history manager
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть пустой после добавления задачи.");

        assertEquals(1, history.size(),
                "После добавления одной задачи в истории должно быть одно событие.");

        // Check if it contains the correct entry
        assertEquals(task, history.get(0),
                "Запись в истории должна совпадать с добавленной задачей.");
    }
}