import manager.TaskManager;
import model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        taskManager.addNewTask(task1);

        Epic epic1 = new Epic(2, "Эпик 1", "Описание эпика 1");
        taskManager.addEpic(epic1);

        Subtask subTask1 = new Subtask(3, "Подзадача 1", "Описание подзадачи", Status.NEW, epic1.getId());
        if (taskManager.addSubtask(subTask1)) {
            System.out.println("Подзадача добавлена успешно.");
        } else {
            System.out.println("Ошибка при добавлении подзадачи.");
        }

        System.out.println("Все задачи: " + taskManager.getTasks());
        System.out.println("Все эпики: " + taskManager.getEpics());
        System.out.println("Подзадачи эпика: " + taskManager.getEpicSubtasks(epic1.getId()));
        System.out.println("История: " + taskManager.getHistory());
    }
}