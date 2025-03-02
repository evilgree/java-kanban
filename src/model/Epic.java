package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
        updateEpicStatus();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    private void updateEpicStatus() {
    }
}