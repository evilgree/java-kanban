package manager;

import model.Task;
import model.Subtask;
import model.Epic;
import model.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private int idCounter = 1;

    private int getNextId() {
        return idCounter++;
    }

    public Task createTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }
        subtask.setId(getNextId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public Task getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    public Subtask getSubtaskById(int subtaskId) {
        return subtasks.get(subtaskId);
    }

    public Epic getEpicById(int epicId) {
        return epics.get(epicId);
    }

    public void updateTask(Task newTask) {
        tasks.put(newTask.getId(), newTask);
    }

    public void updateSubtask(Subtask newSubtask) {
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epics.get(newSubtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void deleteTask(int taskId) {
        tasks.remove(taskId);
    }

    public void deleteSubtask(int subtaskId) {
        Subtask subtask = subtasks.remove(subtaskId);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(subtaskId));
                updateEpicStatus(epic);
            }
        }
    }

    public void deleteEpic(int epicId) {
        Epic epic = epics.remove(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }
    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(Status.NEW);
        }
    }

    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
        }
        epics.clear();
        subtasks.clear();
    }

    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        List<Subtask> epicSubtasks = new ArrayList<>();
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    epicSubtasks.add(subtask);
                }
            }
        }
        return epicSubtasks;
    }

    private void updateEpicStatus(Epic epic) {
        int doneCount = 0;
        int newCount = 0;

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                switch (subtask.getStatus()) {
                    case DONE:
                        doneCount++;
                        break;
                    case NEW:
                        newCount++;
                        break;
                    case IN_PROGRESS:
                        epic.setStatus(Status.IN_PROGRESS);
                        return;
                }
            }
        }

        if (doneCount == epic.getSubtaskIds().size()) {
            epic.setStatus(Status.DONE);
        } else if (newCount == epic.getSubtaskIds().size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}