package manager;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        File file = new File("tasks.csv");
        return FileBackedTaskManager.loadFromFile(file);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}