import java.util.ArrayList;
import java.util.Scanner;

public class TaskManager {

    private ArrayList<Task> tasks;

    private int nextId;
    
    private Scanner scanner;

    // Handles reading/writing the tasks file
    private TaskStorage storage;

    public TaskManager(Scanner scanner, TaskStorage storage) {
        this.scanner = scanner;
        this.storage = storage;

        // Load previously saved tasks when the application starts
        this.tasks = storage.load();
        this.nextId = storage.getNextId();
    }

    // =====================================================
    // CREATE
    // =====================================================
    public void addTask() {
        System.out.println("\n--- Add New Task ---");

        // Keep asking until the user types a non-empty title
        String title = "";
        while (title.isEmpty()) {
            System.out.print("Enter task title: ");
            title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                System.out.println("Error: Task title cannot be empty. Please try again.");
            }
        }

        System.out.print("Enter task description (optional): ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            description = "No description";
        }

        // Create the task with an auto-generated ID, then add it to the list
        Task newTask = new Task(nextId, title, description);
        tasks.add(newTask);
        nextId++;
        boolean saved = saveTasks();

        System.out.println("Task added successfully! (ID: " + newTask.getId() + ")" + unsavedNote(saved));
    }

    // =====================================================
    // READ
    // =====================================================
    public void viewAllTasks() {
        System.out.println("\n--- All Tasks ---");

        if (tasks.isEmpty()) {
            System.out.println("No tasks found. Add a task to get started!");
            return;
        }

        for (Task task : tasks) {
            System.out.println(task);
            System.out.println("------------------------------");
        }
        System.out.println("Total tasks: " + tasks.size());
    }

    // =====================================================
    // UPDATE
    // =====================================================
    public void updateTask() {
        System.out.println("\n--- Update Task ---");

        if (tasks.isEmpty()) {
            System.out.println("No tasks available to update.");
            return;
        }

        int id = readId("Enter the ID of the task to update: ");
        if (id == -1) {
            return; // readId already printed an error message
        }

        Task task = findTaskById(id);
        if (task == null) {
            System.out.println("Error: No task found with ID " + id + ".");
            return;
        }

        System.out.println("\nCurrent task details:");
        System.out.println(task);
        System.out.println("\n(Press Enter to keep the current value.)");

        // Update title
        System.out.print("New title: ");
        String newTitle = scanner.nextLine().trim();
        if (!newTitle.isEmpty()) {
            task.setTitle(newTitle);
        }

        // Update description
        System.out.print("New description: ");
        String newDescription = scanner.nextLine().trim();
        if (!newDescription.isEmpty()) {
            task.setDescription(newDescription);
        }

        // Update status
        System.out.print("New status (1 = Pending, 2 = Completed): ");
        String statusChoice = scanner.nextLine().trim();
        if (statusChoice.equals("1")) {
            task.setStatus(Task.PENDING);
        } else if (statusChoice.equals("2")) {
            task.setStatus(Task.COMPLETED);
        } else if (!statusChoice.isEmpty()) {
            System.out.println("Invalid status choice. Status was not changed.");
        }

        boolean saved = saveTasks();
        System.out.println("Task updated successfully!" + unsavedNote(saved));
    }

    // =====================================================
    // DELETE
    // =====================================================
    public void deleteTask() {
        System.out.println("\n--- Delete Task ---");

        if (tasks.isEmpty()) {
            System.out.println("No tasks available to delete.");
            return;
        }

        int id = readId("Enter the ID of the task to delete: ");
        if (id == -1) {
            return;
        }

        Task task = findTaskById(id);
        if (task == null) {
            System.out.println("Error: No task found with ID " + id + ".");
            return;
        }

        tasks.remove(task);
        boolean saved = saveTasks();
        System.out.println("Task with ID " + id + " deleted successfully!" + unsavedNote(saved));
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Saves all tasks to the file. Returns true if it worked. TaskStorage
     * prints an error message if saving fails, so the program keeps running.
     */
    private boolean saveTasks() {
        return storage.save(tasks, nextId);
    }

    /** Extra text for the success message when the file could not be written. */
    private String unsavedNote(boolean saved) {
        return saved ? "" : " (Note: this change is NOT saved to the file.)";
    }

    /**
     * Searches the list for a task with the given ID.
     * Returns the Task, or null if it does not exist.
     */
    private Task findTaskById(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    /**
     * Reads an ID from the user. If the input is not a number,
     * prints an error and returns -1 (IDs are always 1 or more).
     */
    private int readId(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input. Please enter a numeric ID.");
            return -1;
        }
    }
}
