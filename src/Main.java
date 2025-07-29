import manager.Managers;
import model.Status;
import manager.TaskManager;
import model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic1.getId(), Status.NEW));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", epic1.getId(), Status.NEW));

        Epic epic2 = taskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", epic2.getId(), Status.NEW));

        System.out.println("Все задачи:");
        System.out.println(taskManager.getAllTasks());

        System.out.println("Все подзадачи:");
        System.out.println(taskManager.getAllSubtasks());

        System.out.println("Все эпики:");
        System.out.println(taskManager.getAllEpics());


        taskManager.updateSubtask(new Subtask(subtask1.getTitle(), subtask1.getDescription(), epic1.getId(), Status.DONE));
        taskManager.updateSubtask(new Subtask(subtask2.getTitle(), subtask2.getDescription(), epic1.getId(), Status.NEW));

        System.out.println("Статусы после изменений:");
        System.out.println("Задача 1: " + task1.getStatus());
        System.out.println("Задача 2: " + task2.getStatus());
        System.out.println("Подзадача 1: " + subtask1.getStatus());
        System.out.println("Подзадача 2: " + subtask2.getStatus());

        System.out.println("Статусы эпиков:");
        System.out.println("Эпик 1: " + epic1.getStatus());
        System.out.println("Эпик 2: " + epic2.getStatus());

        taskManager.deleteTask(task1.getId());
        taskManager.deleteEpic(epic2.getId());

        System.out.println("Списки после удаления:");
        System.out.println("Все задачи:");
        System.out.println(taskManager.getAllTasks());

        System.out.println("Все подзадачи:");
        System.out.println(taskManager.getAllSubtasks());

        System.out.println("Все эпики:");
        System.out.println(taskManager.getAllEpics());
    }
}