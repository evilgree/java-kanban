package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {

    void addNewTask(Task task);

    boolean addSubtask(Subtask subtask);

    void addEpic(Epic epic);

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    List<Task> getHistory();

    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getTasks();

    List<Epic> getEpics();
}