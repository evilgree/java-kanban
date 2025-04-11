package model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int id, String title, String description, Status status, int epicId) {
        super(id, title, description, status);
        if (id == epicId) {
            this.epicId = -1;
            return;
        }

        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}