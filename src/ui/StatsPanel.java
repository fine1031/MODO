package ui;

import model.PomodoroStats;
import service.PomodoroService;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class StatsPanel extends JPanel {
    private final PomodoroService pomodoroService;
    private final DefaultTableModel tableModel;

    public StatsPanel(PomodoroService pomodoroService) {
        this.pomodoroService = pomodoroService;
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);

        String[] columns = {"항목", "값"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JButton btnRefresh = new JButton("새로고침");
        btnRefresh.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        btnRefresh.addActionListener(e -> refreshTable());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnRefresh, BorderLayout.SOUTH);
        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        PomodoroStats stats = pomodoroService.getTodayStats();

        int h = stats.getTotalFocusMinutes() / 60;
        int m = stats.getTotalFocusMinutes() % 60;
        String timeStr = (h > 0 ? h + "시간 " : "") + m + "분";
        if (stats.getTotalFocusMinutes() == 0) timeStr = "0분";

        tableModel.addRow(new Object[]{"오늘 총 집중 시간", timeStr});
        tableModel.addRow(new Object[]{"완료한 뽀모도로", stats.getCompletedPomodoros() + "회"});
        tableModel.addRow(new Object[]{"취소한 뽀모도로", stats.getCancelledPomodoros() + "회"});
        tableModel.addRow(new Object[]{"성공률", String.format("%.1f%%", stats.getSuccessRate())});
    }
}
