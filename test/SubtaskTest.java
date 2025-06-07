package test;
import model.Subtask;
import manager.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", 1, Status.NEW);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", 1, Status.NEW);

        subtask1.setId(2);
        subtask2.setId(2);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны.");
    }
}