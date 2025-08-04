package manager;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected HashMap<Integer, Task> tasks = new HashMap<>();
    protected HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected HashMap<Integer, Epic> epics = new HashMap<>();
    protected HistoryManager historyManager;
    protected int nextId = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;

    }

    private int getNextId() {
        return nextId++;
    }

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    @Override
    public Task createTask(Task task) {
        if (isIntersecting(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }
        if (isIntersecting(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
        }
        subtask.setId(getNextId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask.getId());
        updateEpicStatus(epic);
        addToPrioritized(subtask);
        return subtask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Task getTaskById(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void updateTask(Task newTask) {
        if (isIntersecting(newTask)) {
            throw new IllegalArgumentException("Обновляемая задача пересекается по времени с другой задачей");
        }
        Task oldTask = tasks.get(newTask.getId());
        if (oldTask != null) {
            removeFromPrioritized(oldTask);
        }
        tasks.put(newTask.getId(), newTask);
        addToPrioritized(newTask);
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        if (isIntersecting(newSubtask)) {
            throw new IllegalArgumentException("Обновляемая подзадача пересекается по времени с другой задачей");
        }
        Subtask oldSubtask = subtasks.get(newSubtask.getId());
        if (oldSubtask != null) {
            removeFromPrioritized(oldSubtask);
        }
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epics.get(newSubtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
        addToPrioritized(newSubtask);
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void deleteTask(int taskId) {
        Task task = tasks.remove(taskId);
        if (task != null) {
            removeFromPrioritized(task);
            historyManager.remove(taskId);
        }
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask subtask = subtasks.remove(subtaskId);
        if (subtask != null) {
            removeFromPrioritized(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(subtaskId));
                updateEpicStatus(epic);
            }
            historyManager.remove(subtaskId);
        }
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epics.remove(epicId);
        if (epic != null) {
            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    removeFromPrioritized(subtask);
                    historyManager.remove(subtaskId);
                }
            }
            epic.getSubtaskIds().clear();
            historyManager.remove(epicId);
        }
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            removeFromPrioritized(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            removeFromPrioritized(subtask);
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            for (int subtaskId : epic.getSubtaskIds()) {
                historyManager.remove(subtaskId);
            }
        }
        for (Subtask subtask : subtasks.values()) {
            removeFromPrioritized(subtask);
        }
        subtasks.clear();
        epics.clear();
    }

    @Override
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

    protected void updateEpicStatus(Epic epic) {
        int doneCount = 0;
        int newCount = 0;

        for (int subtaskID : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskID);
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
        if (doneCount == epic.getSubtaskIds().size() && doneCount > 0) {
            epic.setStatus(Status.DONE);
        } else if (newCount == epic.getSubtaskIds().size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private boolean isIntersecting(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }
        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        for (Task task : prioritizedTasks) {
            if (task.getId() == newTask.getId()) {
                continue;
            }
            if (task.getStartTime() == null || task.getDuration() == null) {
                continue;
            }
            LocalDateTime start = task.getStartTime();
            LocalDateTime end = task.getEndTime();

            if (newStart.isBefore(end) && newEnd.isAfter(start)) {
                return true;
            }
        }
        return false;
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}