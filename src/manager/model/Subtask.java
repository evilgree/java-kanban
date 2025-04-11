package manager.model;

import manager.Status;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int epicId, Status status) {
        super(title, description, status);
        this.epicId = epicId;
        setStatus(status);
    }

    public int getEpicId() {
        return epicId;
    }
}