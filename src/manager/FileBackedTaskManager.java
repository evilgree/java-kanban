package manager;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }

            writer.newLine();

            writer.write(historyToString(getHistory()));

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл " + file.getAbsolutePath(), e);
        }
    }

    private String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        if (task instanceof Epic) {
            sb.append(TaskType.EPIC);
        } else if (task instanceof Subtask) {
            sb.append(TaskType.SUBTASK);
        } else {
            sb.append(TaskType.TASK);
        }
        sb.append(",");
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

    private String historyToString(List<Task> history) {
        return history.stream()
                .map(t -> String.valueOf(t.getId()))
                .collect(Collectors.joining(","));
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            List<String> lines = Files.readAllLines(file.toPath());

            int i = 0;
            if (!lines.isEmpty() && lines.get(0).equals("id,type,name,status,description,epic")) {
                i++;
            }

            while (i < lines.size() && !lines.get(i).isEmpty()) {
                Task task = fromString(lines.get(i));
                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                }
                if (task.getId() >= manager.idCounter) {
                    manager.idCounter = task.getId() + 1;
                }
                i++;
            }

            i++;

            if (i < lines.size()) {
                List<Integer> historyIds = historyFromString(lines.get(i));
                for (Integer id : historyIds) {
                    Task task = manager.tasks.get(id);
                    if (task == null) {
                        task = manager.epics.get(id);
                    }
                    if (task == null) {
                        task = manager.subtasks.get(id);
                    }
                    if (task != null) {
                        manager.historyManager.add(task);
                    }
                }
            }

            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    if (!epic.getSubtaskIds().contains(subtask.getId())) {
                        epic.addSubtask(subtask.getId());
                    }
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла " + file.getAbsolutePath(), e);
        }

        return manager;
    }

    private static Task fromString(String value) {
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

    private static List<Integer> historyFromString(String value) {
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

    @Override
    public Task createTask(Task task) {
        Task t = super.createTask(task);
        save();
        return t;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic e = super.createEpic(epic);
        save();
        return e;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask s = super.createSubtask(subtask);
        save();
        return s;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int taskId) {
        super.deleteTask(taskId);
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        super.deleteSubtask(subtaskId);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }
}