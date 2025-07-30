package manager;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.List;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String CSV_HEADER = "id,type,name,status,description,epic";

    private final File file;

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(CSVTaskConverter.taskToString(task));
                writer.newLine();
            }

            for (Epic epic : getAllEpics()) {
                writer.write(CSVTaskConverter.taskToString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(CSVTaskConverter.taskToString(subtask));
                writer.newLine();
            }

            writer.newLine();

            writer.write(CSVTaskConverter.historyToString(getHistory()));

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл " + file.getAbsolutePath(), e);
        }
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
                Task task = CSVTaskConverter.fromString(lines.get(i));
                if (task.getType() == TaskType.EPIC) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task.getType() == TaskType.SUBTASK) {
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
                List<Integer> historyIds = CSVTaskConverter.historyFromString(lines.get(i));
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