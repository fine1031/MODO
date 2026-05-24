package service;

import model.Todo;
import storage.DataManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        String trimmed = title.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        todos.add(new Todo(trimmed, false, LocalDate.now().toString()));
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

    public void saveTodos() {
        dataManager.saveTodos(todos);
    }
}
