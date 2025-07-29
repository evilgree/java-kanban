package test;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import manager.HistoryManager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
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

    @Test
    void testDeleteSubtaskRemovesIdFromEpic() {
        Epic epic = taskManager.createEpic(new Epic("Заголовок", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId(), Status.NEW));

        assertTrue(epic.getSubtaskIds().contains(subtask.getId()), "Epic должен содержать id подзадачи");

        taskManager.deleteSubtask(subtask.getId());

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertFalse(updatedEpic.getSubtaskIds().contains(subtask.getId()), "После удаления подзадачи id не должно остаться в эпике");
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача должна быть удалена из менеджера");
    }

    @Test
    void testDeleteEpicRemovesSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Заголовок", "Описание"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание", epic.getId(), Status.NEW));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание", epic.getId(), Status.NEW));

        taskManager.deleteEpic(epic.getId());
        assertNull(taskManager.getEpicById(epic.getId()), "Эпик должен быть удалён");
        assertNull(taskManager.getSubtaskById(subtask1.getId()), "Подзадача 1 должна быть удалена вместе с эпиком");
        assertNull(taskManager.getSubtaskById(subtask2.getId()), "Подзадача 2 должна быть удалена вместе с эпиком");
    }

    @Test
    void testDeleteEpicClearsSubtaskIds() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Desc", epic.getId(), Status.NEW));

        taskManager.deleteEpic(epic.getId());


        assertTrue(epic.getSubtaskIds().isEmpty(), "Список подзадач эпика должен быть пуст после удаления");
    }

    @Test
    void testDeleteAllSubtasksClearsEpicSubtaskIds() {
        Epic epic = taskManager.createEpic(new Epic("Заголовок", "Desc"));
        taskManager.createSubtask(new Subtask("Subtask1", "Desc", epic.getId(), Status.NEW));
        taskManager.createSubtask(new Subtask("Subtask2", "Desc", epic.getId(), Status.NEW));

        taskManager.deleteAllSubtasks();

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "После удаления всех подзадач список id подзадач эпика должен быть пуст");
        assertEquals(Status.NEW, updatedEpic.getStatus(), "Статус эпика должен сбрасываться в NEW после удаления подзадач");
    }

    @Test
    void testUpdateSubtaskUpdatesEpicStatus() {
        Epic epic = taskManager.createEpic(new Epic("Заголовок", "Описание"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание", epic.getId(), Status.NEW));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание", epic.getId(), Status.NEW));


        Subtask updatedSubtask1 = new Subtask(subtask1.getTitle(), subtask1.getDescription(), epic.getId(), Status.DONE);
        updatedSubtask1.setId(subtask1.getId());
        taskManager.updateSubtask(updatedSubtask1);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());


        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus(), "Статус эпика должен обновиться на IN_PROGRESS");
    }

    @Test
    void testHistoryManager_addRemoveMaintainsOrder() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание", Status.NEW));
        Task task3 = taskManager.createTask(new Task("Задача3", "Описание", Status.NEW));


        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getTaskById(task2.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size(), "История должна содержать 3 задачи без дубликатов");
        assertEquals(List.of(task1, task3, task2), history, "Порядок в истории должен сохраняться и обновляться при повторном добавлении");

        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task3.getId());
        List<Task> historyAfterRemove = historyManager.getHistory();
        assertFalse(historyAfterRemove.contains(task3), "Задача должна быть удалена из истории");
    }

    @Test
    void testSettersOnTaskDoNotAffectManagerData() {
        Task task = taskManager.createTask(new Task("Title", "Описание", Status.NEW));
        int id = task.getId();


        task.setTitle("New Title");
        task.setDescription("New Desc");
        task.setStatus(Status.DONE);


        Task fromManager = taskManager.getTaskById(id);


        assertEquals("New Title", fromManager.getTitle(), "Изменения через сеттеры влияют на объект в менеджере");
        assertEquals(Status.DONE, fromManager.getStatus(), "Статус изменился через сеттер");
    }

    @Test
    void testCreateSubtaskWithInvalidEpicIdReturnsNull() {
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", 9999, Status.NEW));
        assertNull(subtask, "Создание подзадачи с несуществующим id эпика должно вернуть null");
    }
}