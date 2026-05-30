package ui;

import model.PomodoroStats;
import storage.DataManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StudyStatsFrame extends JFrame {
    private static final Color MODO_RED = new Color(180, 32, 41);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM");

    private final DataManager dataManager;
    private final JLabel lblTotalMinutes;
    private final JLabel lblCompleted;
    private final JLabel lblCancelled;
    private final JLabel lblSuccessRate;
    private final DefaultTableModel dailyModel;
    private final DefaultTableModel weeklyModel;
    private final DefaultTableModel monthlyModel;

    public StudyStatsFrame(DataManager dataManager) {
        this.dataManager = dataManager;

        setTitle("MODO 학습 통계");
        setSize(980, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MODO_RED);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel title = new JLabel("학습 통계");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        header.add(title, BorderLayout.WEST);

        JButton btnRefresh = new JButton("새로고침");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> refreshStats());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(new Color(245, 245, 245));
        content.setBorder(new EmptyBorder(22, 26, 26, 26));

        lblTotalMinutes = createSummaryValue();
        lblCompleted = createSummaryValue();
        lblCancelled = createSummaryValue();
        lblSuccessRate = createSummaryValue();

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.add(createSummaryCard("누적 집중 시간", lblTotalMinutes));
        summaryPanel.add(createSummaryCard("완료 세션", lblCompleted));
        summaryPanel.add(createSummaryCard("취소 세션", lblCancelled));
        summaryPanel.add(createSummaryCard("성공률", lblSuccessRate));
        content.add(summaryPanel, BorderLayout.NORTH);

        dailyModel = createTableModel("날짜");
        weeklyModel = createTableModel("주간");
        monthlyModel = createTableModel("월간");

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIManager.getFont("Button.font"));
        tabs.addTab("일간 통계", createTablePanel(dailyModel));
        tabs.addTab("주간 통계", createTablePanel(weeklyModel));
        tabs.addTab("월간 통계", createTablePanel(monthlyModel));
        content.add(tabs, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        refreshStats();
    }

    private JLabel createSummaryValue() {
        JLabel label = new JLabel("-", SwingConstants.LEFT);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        label.setForeground(new Color(30, 41, 59));
        return label;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(13, 15, 13, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);
        card.add(titleLabel);
        card.add(valueLabel);
        return card;
    }

    private DefaultTableModel createTableModel(String periodColumn) {
        return new DefaultTableModel(
                new String[]{periodColumn, "집중 시간", "완료", "취소", "성공률"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createTablePanel(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        table.setRowHeight(34);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(createHeaderRenderer());
        table.setDefaultRenderer(Object.class, createBodyRenderer());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225)));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private DefaultTableCellRenderer createHeaderRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBackground(new Color(245, 245, 245));
        renderer.setForeground(new Color(71, 85, 105));
        renderer.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return renderer;
    }

    private DefaultTableCellRenderer createBodyRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return renderer;
    }

    private void refreshStats() {
        Map<String, PomodoroStats> storedStats = dataManager.loadAllStats();
        List<PomodoroStats> statsList = new ArrayList<>(storedStats.values());
        statsList.sort(Comparator.comparing(PomodoroStats::getDate).reversed());

        StatsSummary total = new StatsSummary();
        for (PomodoroStats stats : statsList) {
            total.add(stats);
        }
        lblTotalMinutes.setText(formatMinutes(total.totalMinutes));
        lblCompleted.setText(total.completed + "회");
        lblCancelled.setText(total.cancelled + "회");
        lblSuccessRate.setText(formatRate(total));

        fillDailyTable(statsList);
        fillWeeklyTable(statsList);
        fillMonthlyTable(statsList);
    }

    private void fillDailyTable(List<PomodoroStats> statsList) {
        dailyModel.setRowCount(0);
        for (PomodoroStats stats : statsList) {
            StatsSummary summary = new StatsSummary();
            summary.add(stats);
            dailyModel.addRow(new Object[]{
                    LocalDate.parse(stats.getDate()).format(DATE_FORMAT),
                    formatMinutes(summary.totalMinutes),
                    summary.completed + "회",
                    summary.cancelled + "회",
                    formatRate(summary)
            });
        }
    }

    private void fillWeeklyTable(List<PomodoroStats> statsList) {
        Map<LocalDate, StatsSummary> weekly = new TreeMap<>(Comparator.reverseOrder());
        for (PomodoroStats stats : statsList) {
            LocalDate date = LocalDate.parse(stats.getDate());
            LocalDate monday = date.with(DayOfWeek.MONDAY);
            weekly.computeIfAbsent(monday, ignored -> new StatsSummary()).add(stats);
        }

        weeklyModel.setRowCount(0);
        for (Map.Entry<LocalDate, StatsSummary> entry : weekly.entrySet()) {
            LocalDate monday = entry.getKey();
            String period = monday.format(DATE_FORMAT) + " - "
                    + monday.plusDays(6).format(DATE_FORMAT);
            addSummaryRow(weeklyModel, period, entry.getValue());
        }
    }

    private void fillMonthlyTable(List<PomodoroStats> statsList) {
        Map<YearMonth, StatsSummary> monthly = new TreeMap<>(Comparator.reverseOrder());
        for (PomodoroStats stats : statsList) {
            YearMonth month = YearMonth.from(LocalDate.parse(stats.getDate()));
            monthly.computeIfAbsent(month, ignored -> new StatsSummary()).add(stats);
        }

        monthlyModel.setRowCount(0);
        for (Map.Entry<YearMonth, StatsSummary> entry : monthly.entrySet()) {
            addSummaryRow(monthlyModel, entry.getKey().format(MONTH_FORMAT), entry.getValue());
        }
    }

    private void addSummaryRow(DefaultTableModel model, String period, StatsSummary summary) {
        model.addRow(new Object[]{
                period,
                formatMinutes(summary.totalMinutes),
                summary.completed + "회",
                summary.cancelled + "회",
                formatRate(summary)
        });
    }

    private String formatMinutes(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours == 0) return minutes + "분";
        return hours + "시간 " + minutes + "분";
    }

    private String formatRate(StatsSummary summary) {
        int totalSessions = summary.completed + summary.cancelled;
        if (totalSessions == 0) return "0.0%";
        return String.format("%.1f%%", (double) summary.completed / totalSessions * 100);
    }

    private static class StatsSummary {
        private int totalMinutes;
        private int completed;
        private int cancelled;

        private void add(PomodoroStats stats) {
            totalMinutes += stats.getTotalFocusMinutes();
            completed += stats.getCompletedPomodoros();
            cancelled += stats.getCancelledPomodoros();
        }
    }

}
