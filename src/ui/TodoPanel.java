package ui;

import model.Todo;
import service.TodoService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public class TodoPanel extends JPanel {
    private final TodoService todoService;
    private final JPanel listPanel;
    private final JTextField inputField;

    public TodoPanel(TodoService todoService) {
        this.todoService = todoService;
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setOpaque(false);
        inputField = new JTextField();
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        JButton addButton = new JButton("추가");
        addButton.addActionListener(e -> addTodo());
        inputField.addActionListener(e -> addTodo());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        refreshTodos();
    }

    private void addTodo() {
        todoService.addTodo(inputField.getText());
        inputField.setText("");
        refreshTodos();
    }

    private void refreshTodos() {
        listPanel.removeAll();
        List<Todo> todos = todoService.getTodos();

        if (todos.isEmpty()) {
            JLabel emptyLabel = new JLabel("오늘 할 일을 추가하세요.");
            emptyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            emptyLabel.setForeground(Color.GRAY);
            listPanel.add(emptyLabel);
        }

        for (int i = 0; i < todos.size(); i++) {
            listPanel.add(createTodoRow(todos.get(i), i));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createTodoRow(Todo todo, int index) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JCheckBox checkBox = new JCheckBox(todo.getTitle(), todo.isCompleted());
        checkBox.setOpaque(false);
        checkBox.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        checkBox.addActionListener(e -> {
            todoService.toggleTodo(index);
            refreshTodos();
        });

        JButton deleteButton = new JButton("삭제");
        deleteButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        deleteButton.addActionListener(e -> {
            todoService.removeTodo(index);
            refreshTodos();
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(deleteButton);

        row.add(checkBox, BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);
        return row;
    }
}
