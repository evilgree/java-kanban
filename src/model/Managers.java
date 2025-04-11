package model;

import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }
}