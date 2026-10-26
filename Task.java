public class Task {

    public static final String PENDING = "Pending";
    public static final String COMPLETED = "Completed";

    private int id;
    private String title;
    private String description;
    private String status;

    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = PENDING;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ID          : " + id + "\n"
             + "Title       : " + title + "\n"
             + "Description : " + description + "\n"
             + "Status      : " + status;
    }
}
