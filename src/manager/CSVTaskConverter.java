package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class CSVTaskConverter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String taskToString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getTitle()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId());
        } else {
            sb.append("");
        }
        sb.append(",");
        if (task.getStartTime() != null) {
            sb.append(task.getStartTime().format(DATE_TIME_FORMATTER));
        }
        sb.append(",");
        if (task.getDuration() != null) {
            sb.append(task.getDuration().toMinutes());
        }
        return sb.toString();
    }

    public static String historyToString(List<Task> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }
        return history.stream()
                .map(t -> String.valueOf(t.getId()))
                .collect(Collectors.joining(","));
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",", 8);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Некорректная строка задачи: " + value);
        }

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        String epicField = parts[5];

        LocalDateTime startTime = null;
        if (parts.length > 6 && !parts[6].isEmpty()) {
            startTime = LocalDateTime.parse(parts[6], DATE_TIME_FORMATTER);
        }

        Duration duration = null;
        if (parts.length > 7 && !parts[7].isEmpty()) {
            long minutes = Long.parseLong(parts[7]);
            duration = Duration.ofMinutes(minutes);
        }

        switch (type) {
            case TASK:
                Task task = new Task(title, description, status);
                task.setId(id);
                task.setStartTime(startTime);
                task.setDuration(duration);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStartTime(startTime);
                epic.setDuration(duration);
                return epic;
            case SUBTASK:
                int epicId = epicField.isEmpty() ? -1 : Integer.parseInt(epicField);
                Subtask subtask = new Subtask(title, description, epicId, status);
                subtask.setId(id);
                subtask.setStartTime(startTime);
                subtask.setDuration(duration);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    public static List<Integer> historyFromString(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = value.split(",");
        List<Integer> historyIds = new ArrayList<>();
        for (String part : parts) {
            historyIds.add(Integer.parseInt(part));
        }
        return historyIds;
    }
}