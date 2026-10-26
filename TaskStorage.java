import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TaskStorage.java
 * Handles saving tasks to a text file and loading them back.
 *
 * File format (UTF-8 text, one task per line):
 *   #NEXT_ID=4                      <- first line: the next ID to hand out
 *   id|title|description|status     <- one line per task
 *
 * Special characters inside text are escaped so they cannot break the format:
 *   \  becomes \\     |  becomes \p     new line becomes \n
 */
public class TaskStorage {

    private static final String HEADER_PREFIX = "#NEXT_ID=";

    private final Path filePath;

    // The next ID read from the file (valid after load() finishes)
    private int nextId = 1;

    // Safety switch: if the file exists but could not be READ, we turn saving
    // off so we never overwrite the user's data with an empty list.
    private boolean savingEnabled = true;

    public TaskStorage(String fileName) {
        this.filePath = Paths.get(fileName);
    }

    public int getNextId() {
        return nextId;
    }

    public String getFileName() {
        return filePath.toString();
    }

    // =====================================================
    // LOAD
    // =====================================================

    /**
     * Reads tasks from the file. Never throws: problems are reported with
     * messages and the application continues with whatever could be loaded.
     */
    public ArrayList<Task> load() {
        ArrayList<Task> tasks = new ArrayList<>();

        // First run: no file yet. Start empty; the file is created on first save.
        if (!Files.exists(filePath)) {
            System.out.println("No saved tasks found (" + filePath + "). Starting with an empty list.");
            return tasks;
        }

        int headerNextId = 0;
        int skipped = 0;
        int lineNumber = 0;
        Set<Integer> seenIds = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // ignore blank lines
                }

                // Header line with the next ID counter
                if (line.startsWith(HEADER_PREFIX)) {
                    try {
                        headerNextId = Integer.parseInt(line.substring(HEADER_PREFIX.length()).trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: invalid ID counter on line " + lineNumber + ". Ignoring it.");
                    }
                    continue;
                }

                Task task = parseTask(line, seenIds);
                if (task == null) {
                    System.out.println("Warning: skipped invalid data on line " + lineNumber + ".");
                    skipped++;
                } else {
                    tasks.add(task);
                    seenIds.add(task.getId());
                }
            }
        } catch (IOException e) {
            // Could not read the file at all. Do NOT allow saving, so the
            // original file is left untouched.
            System.out.println("Error: could not read '" + filePath + "': " + e.getMessage());
            System.out.println("Starting with an empty list. Saving is DISABLED to protect your existing file.");
            savingEnabled = false;
            tasks.clear();
            nextId = 1;
            return tasks;
        }

        // Next ID must be higher than every ID in use, and never go backwards
        int maxId = 0;
        for (Task t : tasks) {
            if (t.getId() > maxId) {
                maxId = t.getId();
            }
        }
        nextId = Math.max(headerNextId, maxId + 1);

        // If some lines were bad, keep a copy of the original file before it is rewritten
        if (skipped > 0) {
            backupOriginalFile();
        }

        System.out.println("Loaded " + tasks.size() + " task(s) from " + filePath + ".");
        return tasks;
    }

    /**
     * Turns one line into a Task, or returns null if the line is invalid.
     */
    private Task parseTask(String line, Set<Integer> seenIds) {
        // limit -1 keeps empty trailing fields (e.g. an empty description)
        String[] parts = line.split("\\|", -1);
        if (parts.length != 4) {
            return null;
        }

        int id;
        try {
            id = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
        if (id <= 0 || seenIds.contains(id)) {
            return null; // IDs must be positive and unique
        }

        String title = unescape(parts[1]).trim();
        String description = unescape(parts[2]);
        String status = unescape(parts[3]).trim();

        if (title.isEmpty()) {
            return null;
        }
        if (!status.equals(Task.PENDING) && !status.equals(Task.COMPLETED)) {
            return null;
        }
        if (description.trim().isEmpty()) {
            description = "No description";
        }

        Task task = new Task(id, title, description);
        task.setStatus(status);
        return task;
    }

    // =====================================================
    // SAVE
    // =====================================================

    /**
     * Writes all tasks to the file. Returns true on success, false on failure.
     * It writes to a temporary file first and then replaces the real file,
     * so a crash in the middle of saving cannot leave a half-written file.
     */
    public boolean save(List<Task> tasks, int nextIdToSave) {
        if (!savingEnabled) {
            System.out.println("Warning: saving is disabled because the task file could not be read earlier.");
            return false;
        }

        Path tempPath = Paths.get(filePath.toString() + ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
                writer.write(HEADER_PREFIX + nextIdToSave);
                writer.newLine();
                for (Task t : tasks) {
                    writer.write(t.getId() + "|"
                            + escape(t.getTitle()) + "|"
                            + escape(t.getDescription()) + "|"
                            + escape(t.getStatus()));
                    writer.newLine();
                }
            }
            Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("Error: could not save tasks to '" + filePath + "': " + e.getMessage());
            System.out.println("Your changes are still in memory but are NOT saved to disk.");
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
                // nothing more we can do
            }
            return false;
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    /** Copies the original file to <name>.bak so bad lines are not lost forever. */
    private void backupOriginalFile() {
        Path backup = Paths.get(filePath.toString() + ".bak");
        try {
            Files.copy(filePath, backup, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("A backup of the original file was saved as '" + backup + "'.");
        } catch (IOException e) {
            System.out.println("Warning: could not create a backup file: " + e.getMessage());
        }
    }

    /** Makes text safe to store on a single line. */
    private String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '|') {
                sb.append("\\p");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Reverses escape(). */
    private String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == '\\') {
                    sb.append('\\');
                } else if (next == 'p') {
                    sb.append('|');
                } else if (next == 'n') {
                    sb.append('\n');
                } else if (next == 'r') {
                    sb.append('\r');
                } else {
                    sb.append(c).append(next); // unknown sequence: keep as is
                }
                i++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
