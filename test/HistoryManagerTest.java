package test;
import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import model.Task;
import manager.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testAddToHistory() {
        Task task = new Task("Историческая Задача", "Историческое Описание", Status.NEW);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "После добавления задачи история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи история должна содержать одну задачу.");
    }

    @Test
    void testRemoveFromBeginning() {
        Task t1 = new Task("t1", "d", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("t2", "d", Status.NEW);
        t2.setId(2);
        Task t3 = new Task("t3", "d", Status.NEW);
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(t1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(t1));
        assertEquals(List.of(t2, t3), history);
    }

    @Test
    void testRemoveFromMiddle() {
        Task t1 = new Task("t1", "d", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("t2", "d", Status.NEW);
        t2.setId(2);
        Task t3 = new Task("t3", "d", Status.NEW);
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(t2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(t2));
        assertEquals(List.of(t1, t3), history);
    }

    @Test
    void testRemoveFromEnd() {
        Task t1 = new Task("t1", "d", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("t2", "d", Status.NEW);
        t2.setId(2);
        Task t3 = new Task("t3", "d", Status.NEW);
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(t3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(t3));
        assertEquals(List.of(t1, t2), history);
    }
}