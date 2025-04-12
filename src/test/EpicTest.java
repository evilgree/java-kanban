package test;
import model.Epic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void testEpicCannotAddItselfAsSubtask() {
        Epic epic = new Epic(1, "Эпик Заголовок", "Эпик Описание");

        assertFalse(epic.addSubtask(epic.getId()), "Эпик не должен иметь возможность добавлять себя как подзадачу.");
    }
}