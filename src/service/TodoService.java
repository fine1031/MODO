package service;

import model.Todo;
import storage.DataManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TodoService {
    private final DataManager dataManager;
    private final List<Todo> todos;

    public TodoService(DataManager dataManager) {
        this.dataManager = dataManager;
        this.todos = new ArrayList<>(dataManager.loadTodos());
    }

    public List<Todo> getTodos() {
        return Collections.unmodifiableList(todos);
    }

    public void addTodo(String title) {
        addTodo(title, LocalDate.now().toString());
    }

    public List<Todo> getTodosByDate(String targetDate) {
        return todos.stream()
                .filter(todo -> targetDate.equals(todo.getTargetDate()))
                .collect(Collectors.toUnmodifiableList());
    }

    public void addTodo(String title, String targetDate) {
        String trimmed = title.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        todos.add(new Todo(trimmed, false, targetDate));
        saveTodos();
    }

    public void removeTodo(int index) {
        if (index < 0 || index >= todos.size()) {
            return;
        }
        todos.remove(index);
        saveTodos();
    }

    public void toggleTodo(int index) {
        if (index < 0 || index >= todos.size()) {
            return;
        }
        Todo todo = todos.get(index);
        todo.setCompleted(!todo.isCompleted());
        saveTodos();
    }

    public void removeTodo(Todo todo) {
        todos.remove(todo);
        saveTodos();
    }

    public void toggleTodo(Todo todo) {
        todo.setCompleted(!todo.isCompleted());
        saveTodos();
    }

    public void saveTodos() {
        dataManager.saveTodos(todos);
    }
}
