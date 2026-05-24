package ui;

import model.PomodoroStats;
import storage.DataManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

public class CalendarMemoPanel extends JPanel {
    private static final Color COLOR_TODAY     = new Color(180, 32, 41);
    private static final Color COLOR_HAS_STATS = new Color(254, 226, 226);
    private static final Color COLOR_SELECTED  = new Color(59, 130, 246);
    private static final Color COLOR_DEFAULT   = Color.WHITE;

    private final JLabel lblSelectedInfo;
    private JButton lastSelected = null;
    private Color lastSelectedOriginalColor = COLOR_DEFAULT;

    public CalendarMemoPanel(DataManager dataManager) {
        setLayout(new BorderLayout(0, 8));
        setBackground(Color.WHITE);

        Map<String, PomodoroStats> statsMap = dataManager.loadAllStats();

        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        JPanel headerPanel = new JPanel(new GridLayout(1, 7));
        headerPanel.setBackground(Color.WHITE);
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            headerPanel.add(lbl);
        }

        JPanel gridPanel = new JPanel(new GridLayout(6, 7, 2, 2));
        gridPanel.setBackground(new Color(245, 245, 245));
        buildCalendarGrid(gridPanel, statsMap);

        lblSelectedInfo = new JLabel("날짜를 클릭하면 해당 날의 기록을 볼 수 있어요.", SwingConstants.CENTER);
        lblSelectedInfo.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        lblSelectedInfo.setForeground(Color.GRAY);

        add(headerPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(lblSelectedInfo, BorderLayout.SOUTH);
    }

    private void buildCalendarGrid(JPanel gridPanel, Map<String, PomodoroStats> statsMap) {
        YearMonth ym = YearMonth.now();
        int startDow = ym.atDay(1).getDayOfWeek().getValue() % 7;
        int daysInMonth = ym.lengthOfMonth();
        String today = LocalDate.now().toString();

        for (int i = 0; i < startDow; i++) {
            gridPanel.add(new JLabel());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            String dateStr = ym.atDay(day).toString();
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);

            Color baseColor;
            if (dateStr.equals(today)) {
                baseColor = COLOR_TODAY;
                btn.setForeground(Color.WHITE);
            } else if (statsMap.containsKey(dateStr)) {
                baseColor = COLOR_HAS_STATS;
                btn.setForeground(Color.BLACK);
            } else {
                baseColor = COLOR_DEFAULT;
                btn.setForeground(Color.BLACK);
            }
            btn.setBackground(baseColor);

            final String d = dateStr;
            final Color originalColor = baseColor;
            btn.addActionListener(e -> {
                // 이전 선택 버튼 색 복원
                if (lastSelected != null) {
                    lastSelected.setBackground(lastSelectedOriginalColor);
                    lastSelected.setForeground(lastSelectedOriginalColor.equals(COLOR_TODAY) ? Color.WHITE : Color.BLACK);
                }
                // 현재 버튼 선택 표시
                btn.setBackground(COLOR_SELECTED);
                btn.setForeground(Color.WHITE);
                lastSelected = btn;
                lastSelectedOriginalColor = originalColor;

                showDayInfo(d, statsMap);
            });

            gridPanel.add(btn);
        }
    }

    private void showDayInfo(String dateStr, Map<String, PomodoroStats> statsMap) {
        PomodoroStats stats = statsMap.get(dateStr);
        if (stats == null) {
            lblSelectedInfo.setForeground(Color.GRAY);
            lblSelectedInfo.setText(dateStr + " — 기록 없음");
        } else {
            int h = stats.getTotalFocusMinutes() / 60;
            int m = stats.getTotalFocusMinutes() % 60;
            lblSelectedInfo.setForeground(new Color(30, 41, 59));
            lblSelectedInfo.setText(String.format(
                    "%s — 뽀모도로 %d회 | 집중 %d시간 %d분",
                    dateStr, stats.getCompletedPomodoros(), h, m
            ));
        }
    }
}
