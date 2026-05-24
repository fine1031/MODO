package service;

import model.PomodoroStats;
import storage.DataManager;

public class PomodoroService {
    private final DataManager dataManager;
    private PomodoroStats todayStats;

    public PomodoroService(DataManager dataManager) {
        this.dataManager = dataManager;
        this.todayStats = dataManager.loadTodayStats();
    }

    public PomodoroStats getTodayStats() {
        return todayStats;
    }

    public void completePomodoro(int focusMinutes) {
        todayStats.addFocusSession(focusMinutes);
        dataManager.saveStats(todayStats);
    }

    public void cancelPomodoro() {
        todayStats.cancelPomodoro();
        dataManager.saveStats(todayStats);
    }
}
