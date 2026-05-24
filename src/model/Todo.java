package model;

public class Todo {
    private String title;
    private boolean completed;
    private String targetDate;

    public Todo(String title, boolean completed, String targetDate) {
        this.title = title;
        this.completed = completed;
        this.targetDate = targetDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getTargetDate() {
        return targetDate;
    }
}
