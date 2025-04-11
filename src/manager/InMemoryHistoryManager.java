package manager;

import model.HistoryManager;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();
    private final Map<Integer, Task> tasksMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (!tasksMap.containsKey(task.getId())) {
            history.add(task);
            tasksMap.put(task.getId(), task);

            if (history.size() > 10) {
                tasksMap.remove(history.remove(0).getId());
            } } }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

}