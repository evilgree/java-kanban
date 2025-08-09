package manager;

import model.Task;
import model.Subtask;
import model.Epic;

import java.util.List;

public interface TaskManager {

    Task createTask(Task task);

    Subtask createSubtask(Subtask subtask);

    Epic createEpic(Epic epic);

    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    Task getTaskById(int taskId);

    Subtask getSubtaskById(int subtaskId);

    Epic getEpicById(int epicId);

    void updateTask(Task newTask);

    void updateSubtask(Subtask newSubtask);

    void updateEpic(Epic epic);

    void deleteTask(int taskId);

    void deleteSubtask(int subtaskId);

    void deleteEpic(int epicId);

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    List<Subtask> getSubtasksByEpicId(int epicId);

    List<Task> getHistory();

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    boolean hasIntersection(Epic epic);

    List<Task> getPrioritizedTasks();

    boolean hasIntersection(Subtask subtask);
}