# Task Management Console Application (Java) – with Persistent Storage

**Cognifyz Technologies – Software Development Internship**
- Level 2 – Task 3: Console CRUD application
- Level 3 – Task 5: Persistent storage using File I/O

## What the project does
A console Task Manager written in plain Java. You can **Create, Read, Update and Delete** tasks from a menu. Tasks are stored in an in-memory `ArrayList<Task>` **and saved to a text file (`tasks.txt`)**, so they are still there when you close the program and run it again.

## Features
- Add, view, update and delete tasks (ID, title, description, status Pending/Completed)
- Unique auto-generated IDs that stay unique after restarts (and are never reused after a delete)
- Empty titles, invalid menu choices, invalid or unknown IDs are handled without crashing
- **Automatic load** of saved tasks at startup
- **Automatic save** after every add, update and delete
- Safe handling of missing, empty, corrupted or unreadable task files

## Technologies used
- Java (JDK 8 or later) – standard library only (`java.nio.file`, `java.util`)
- No database, no framework, no external dependencies

## Project structure
```
TaskManager/
├── src/
│   ├── Task.java          # Task model (unchanged from Task 3)
│   ├── TaskManager.java   # CRUD logic; loads on start, saves after changes
│   ├── TaskStorage.java   # NEW: reads/writes the tasks file
│   └── Main.java          # Entry point and menu loop
├── .gitignore
└── README.md
```

## How file-based persistence works
1. **On startup**, `TaskManager` asks `TaskStorage.load()` to read `tasks.txt`. If the file does not exist (first run), the app starts with an empty list; the file is created the first time you save.
2. **After every Add / Update / Delete**, `TaskStorage.save()` rewrites the whole file from the in-memory list.
3. **Safe writing:** data is first written to `tasks.txt.tmp` and then moved over `tasks.txt`, so a failure mid-write cannot leave a half-written file.
4. **IDs:** the file's first line stores the next ID to hand out, so IDs stay unique and are not reused even if you delete the newest task and restart.

### File format
Plain UTF-8 text, one task per line:
```
#NEXT_ID=4
1|Buy groceries|Milk, eggs|Pending
2|Study Java|No description|Completed
3|Fix bug|Check login page|Pending
```
Fields: `id|title|description|status`. Special characters are escaped so they cannot break the format: `\` is stored as `\\`, `|` as `\p`, and a new line as `\n`.

### Error handling
| Situation | What happens |
|---|---|
| File missing (first run) | Starts empty; file created on first save |
| File empty | Starts empty, no error |
| Some invalid lines (bad ID, bad status, empty title, duplicate ID, wrong field count) | Invalid lines are skipped with a warning naming the line; valid tasks still load. The original file is first copied to `tasks.txt.bak` so nothing is lost |
| File cannot be read (e.g. permission problem) | Error message; app starts empty and **saving is disabled** so the existing file is never overwritten by an empty list |
| File cannot be written (e.g. disk full, no permission) | Error message; the app keeps running and tells you the change is not saved to disk |

## Where task data is stored
In `tasks.txt` in the **folder you run the program from** (the project folder if you follow the steps below). You can use another file by passing a path as an argument: `java -cp out Main myTasks.txt`.

## Installation
No dependencies to install. You only need a JDK: check with `java -version` and `javac -version`.

## How to run (terminal)
From the project folder (`TaskManager/`):
```bash
mkdir out                      # first time only (Windows: mkdir out)
javac -d out src/*.java        # compile
java -cp out Main              # run
```

## How to test persistent storage
1. Run the program, choose **1** and add two or three tasks, then choose **5** (Exit).
2. Open `tasks.txt` in the project folder and check your tasks are there.
3. Run `java -cp out Main` again. It should print `Loaded 3 task(s) from tasks.txt.`; choose **2** to confirm every task is back.
4. Choose **3**, change a task's status or title, Exit, run again, and confirm the change stayed.
5. Choose **4**, delete a task, Exit, run again, and confirm it is still gone. Add a new task and notice its ID is **not** reused.
6. **No file:** delete `tasks.txt` and run again. It starts empty with no crash.
7. **Bad data:** open `tasks.txt`, add a line like `hello world`, and run again. You will see a warning, valid tasks still load, and a `tasks.txt.bak` backup is created.

## Sample output
```
No saved tasks found (tasks.txt). Starting with an empty list.
=== MENU ===  1
--- Add New Task ---
Enter task title: Buy groceries
Enter task description (optional): Milk, eggs
Task added successfully! (ID: 1)
... (Exit, then run again) ...
Loaded 1 task(s) from tasks.txt.
--- All Tasks ---
ID          : 1
Title       : Buy groceries
Description : Milk, eggs
Status      : Pending
------------------------------
Total tasks: 1
```

## Limitations
- The whole file is rewritten on each change (fine for a small task list).
- The file is not locked, so don't run two copies of the program on the same file at once.
