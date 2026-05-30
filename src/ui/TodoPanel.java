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
import javax.swing.Icon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TodoPanel extends JPanel {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final Color CHECK_COLOR = new Color(71, 85, 105);
    private static final Icon UNCHECKED_ICON = new TodoCheckIcon(false);
    private static final Icon CHECKED_ICON = new TodoCheckIcon(true);

    private final TodoService todoService;
    private final JPanel listPanel;
    private final JTextField inputField;
    private final JLabel dateLabel;
    private String selectedDate = LocalDate.now().toString();

    public TodoPanel(TodoService todoService, JLabel dateLabel) {
        this.todoService = todoService;
        this.dateLabel = dateLabel;
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
        todoService.addTodo(inputField.getText(), selectedDate);
        inputField.setText("");
        refreshTodos();
    }

    private void refreshTodos() {
        listPanel.removeAll();
        dateLabel.setText(formatSelectedDate());
        List<Todo> todos = todoService.getTodosByDate(selectedDate);

        if (todos.isEmpty()) {
            JLabel emptyLabel = new JLabel("선택한 날짜의 할 일을 추가하세요.");
            emptyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            emptyLabel.setForeground(Color.GRAY);
            listPanel.add(emptyLabel);
        }

        for (int i = 0; i < todos.size(); i++) {
            listPanel.add(createTodoRow(todos.get(i)));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    public void showTodosForDate(String targetDate) {
        selectedDate = targetDate;
        refreshTodos();
    }

    private String formatSelectedDate() {
        LocalDate date = LocalDate.parse(selectedDate);
        String suffix = date.equals(LocalDate.now()) ? " (오늘)" : "";
        return date.format(DISPLAY_DATE) + suffix;
    }

    private JPanel createTodoRow(Todo todo) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JCheckBox checkBox = new JCheckBox(formatTodoTitle(todo), todo.isCompleted());
        checkBox.setOpaque(false);
        checkBox.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        checkBox.setIcon(UNCHECKED_ICON);
        checkBox.setSelectedIcon(CHECKED_ICON);
        checkBox.addActionListener(e -> {
            todoService.toggleTodo(todo);
            refreshTodos();
        });

        JButton deleteButton = new JButton("삭제");
        deleteButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        deleteButton.addActionListener(e -> {
            todoService.removeTodo(todo);
            refreshTodos();
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(deleteButton);

        row.add(checkBox, BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);
        return row;
    }

    private String formatTodoTitle(Todo todo) {
        if (!todo.isCompleted()) return todo.getTitle();
        return "<html><font color='#000000'><strike>" + todo.getTitle() + "</strike></font></html>";
    }

    private static class TodoCheckIcon implements Icon {
        private static final int SIZE = 16;
        private final boolean selected;

        private TodoCheckIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(selected ? CHECK_COLOR : Color.WHITE);
            g2.fillRoundRect(x, y, SIZE, SIZE, 4, 4);
            g2.setColor(CHECK_COLOR);
            g2.drawRoundRect(x, y, SIZE, SIZE, 4, 4);

            if (selected) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 4, y + 8, x + 7, y + 11);
                g2.drawLine(x + 7, y + 11, x + 12, y + 5);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE + 2;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }
}
