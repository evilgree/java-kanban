package model;

import manager.Status;
import manager.TaskType;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    public boolean addSubtask(int subtaskId) {
        if (subtaskId == getId()) {
            return false;
        }
        subtaskIds.add(subtaskId);
        return true;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
}