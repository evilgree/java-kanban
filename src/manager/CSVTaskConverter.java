package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CSVTaskConverter {

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
        return sb.toString();
    }

    public static String historyToString(List<Task> history) {
        return history.stream()
                .map(t -> String.valueOf(t.getId()))
                .collect(Collectors.joining(","));
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",", 6);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        String epicField = parts.length > 5 ? parts[5] : "";

        switch (type) {
            case TASK:
                Task task = new Task(title, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                return epic;
            case SUBTASK:
                int epicId = epicField.isEmpty() ? -1 : Integer.parseInt(epicField);
                Subtask subtask = new Subtask(title, description, epicId, status);
                subtask.setId(id);
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
