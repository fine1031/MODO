package storage;

import model.PomodoroStats;
import model.Todo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final Path DATA_DIR = Path.of("data");
    private static final Path TODO_FILE = DATA_DIR.resolve("todo_list.txt");
    private static final Path STATS_FILE = DATA_DIR.resolve("pomodoro_stats.txt");

    public List<Todo> loadTodos() {
        List<Todo> todos = new ArrayList<>();
        if (!Files.exists(TODO_FILE)) {
            return todos;
        }

        try (BufferedReader reader = Files.newBufferedReader(TODO_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 3) {
                    todos.add(new Todo(parts[0], Boolean.parseBoolean(parts[1]), parts[2]));
                }
            }
        } catch (IOException e) {
            System.out.println("TODO 데이터를 불러오지 못했습니다.");
        }
        return todos;
    }

    public void saveTodos(List<Todo> todos) {
        ensureDataDir();
        try (BufferedWriter writer = Files.newBufferedWriter(TODO_FILE, StandardCharsets.UTF_8)) {
            for (Todo todo : todos) {
                writer.write(todo.getTitle() + "|" + todo.isCompleted() + "|" + todo.getTargetDate());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("TODO 데이터를 저장하지 못했습니다.");
        }
    }

    public PomodoroStats loadTodayStats() {
        String today = LocalDate.now().toString();
        if (!Files.exists(STATS_FILE)) {
            return new PomodoroStats(today, 0, 0);
        }

        try (BufferedReader reader = Files.newBufferedReader(STATS_FILE, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null) {
                return new PomodoroStats(today, 0, 0);
            }

            String[] parts = line.split("\\|", -1);
            if (parts.length >= 3 && today.equals(parts[0])) {
                return new PomodoroStats(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("통계 데이터를 불러오지 못했습니다.");
        }
        return new PomodoroStats(today, 0, 0);
    }

    public void saveStats(PomodoroStats stats) {
        ensureDataDir();
        try (BufferedWriter writer = Files.newBufferedWriter(STATS_FILE, StandardCharsets.UTF_8)) {
            writer.write(stats.getDate() + "|" + stats.getCompletedPomodoros() + "|" + stats.getTotalFocusMinutes());
        } catch (IOException e) {
            System.out.println("통계 데이터를 저장하지 못했습니다.");
        }
    }

    private void ensureDataDir() {
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            System.out.println("data 폴더를 만들지 못했습니다.");
        }
    }
}
