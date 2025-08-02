package model;

import manager.Status;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

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
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateTimeAndDuration(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            duration = Duration.ZERO;
            startTime = null;
            endTime = null;
            return;
        }

        duration = Duration.ZERO;
        startTime = null;
        endTime = null;

        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() == null || subtask.getDuration() == null) {
                continue;
            }
            duration = duration.plus(subtask.getDuration());

            if (startTime == null || subtask.getStartTime().isBefore(startTime)) {
                startTime = subtask.getStartTime();
            }

            LocalDateTime subtaskEnd = subtask.getEndTime();
            if (endTime == null || (subtaskEnd != null && subtaskEnd.isAfter(endTime))) {
                endTime = subtaskEnd;
            }
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
}