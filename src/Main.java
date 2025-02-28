public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        // Создание задач
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание задачи 2"));

        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", epic1.getId()));

        Epic epic2 = taskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", epic2.getId()));
    }
}
