package model;

import manager.Status;
import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int epicId, Status status) {
        super(title, description, status);
        this.epicId = epicId;
        setStatus(status);
    }

    public Subtask(String title, String description, int epicId, Status status, Duration duration, LocalDateTime startTime) {
        super(title, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}