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
}