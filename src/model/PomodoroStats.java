package model;

public class PomodoroStats {
    private String date;
    private int completedPomodoros;
    private int totalFocusMinutes;

    public PomodoroStats(String date, int completedPomodoros, int totalFocusMinutes) {
        this.date = date;
        this.completedPomodoros = completedPomodoros;
        this.totalFocusMinutes = totalFocusMinutes;
    }

    public String getDate() {
        return date;
    }

    public int getCompletedPomodoros() {
        return completedPomodoros;
    }

    public int getTotalFocusMinutes() {
        return totalFocusMinutes;
    }

    public void addFocusSession(int minutes) {
        completedPomodoros++;
        totalFocusMinutes += minutes;
    }
}
