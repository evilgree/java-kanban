package test;
import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import model.Task;
import manager.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void addAndGetHistory_noDuplicatesAndOrderMaintained() {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание 3", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(List.of(task1, task3, task2), history);
    }

    @Test
    void removeTaskFromHistory_removesCorrectly() {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание 3", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertFalse(history.contains(task2));
        assertEquals(List.of(task1, task3), history);
    }

    @Test
    void addNullTask_doesNotAddToHistory() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty());
    }
}