package model;

public class PomodoroStats {
    private String date;
    private int completedPomodoros;
    private int cancelledPomodoros;
    private int totalFocusMinutes;

    public PomodoroStats(String date, int completedPomodoros, int cancelledPomodoros, int totalFocusMinutes) {
        this.date = date;
        this.completedPomodoros = completedPomodoros;
        this.cancelledPomodoros = cancelledPomodoros;
        this.totalFocusMinutes = totalFocusMinutes;
    }

    public String getDate() { return date; }
    public int getCompletedPomodoros() { return completedPomodoros; }
    public int getCancelledPomodoros() { return cancelledPomodoros; }
    public int getTotalFocusMinutes() { return totalFocusMinutes; }

    public void addFocusSession(int minutes) {
        completedPomodoros++;
        totalFocusMinutes += minutes;
    }

    public void cancelPomodoro() { cancelledPomodoros++; }

    public double getSuccessRate() {
        int total = completedPomodoros + cancelledPomodoros;
        if (total == 0) return 0.0;
        return (double) completedPomodoros / total * 100;
    }
}
