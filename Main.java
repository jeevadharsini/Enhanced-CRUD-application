import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String fileName = (args.length > 0) ? args[0] : "tasks.txt";
        TaskStorage storage = new TaskStorage(fileName);

        System.out.println("=================================");
        System.out.println("   Welcome to Task Manager App   ");
        System.out.println("=================================");

        TaskManager manager = new TaskManager(scanner, storage);
        boolean running = true;

        try {
            
            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        manager.addTask();
                        break;
                    case "2":
                        manager.viewAllTasks();
                        break;
                    case "3":
                        manager.updateTask();
                        break;
                    case "4":
                        manager.deleteTask();
                        break;
                    case "5":
                        running = false;
                        System.out.println("\nThank you for using Task Manager. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice! Please enter a number from 1 to 5.");
                }
            }
        } catch (NoSuchElementException e) {
            
            System.out.println("\nInput closed. Exiting application.");
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n========== MENU ==========");
        System.out.println("1. Add Task");
        System.out.println("2. View All Tasks");
        System.out.println("3. Update Task");
        System.out.println("4. Delete Task");
        System.out.println("5. Exit");
        System.out.print("Enter your choice (1-5): ");
    }
}
