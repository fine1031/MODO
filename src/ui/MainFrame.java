package ui;

import model.PomodoroStats;
import service.PomodoroService;
import service.TodoService;
import storage.DataManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {
    private JLabel lblDailyGoal;
    private JLabel lblTotalTime;
    private JLabel lblPomodoroCount;
    private final PomodoroService pomodoroService;

    public MainFrame() {
        DataManager dataManager = new DataManager();
        TodoService todoService = new TodoService(dataManager);
        pomodoroService = new PomodoroService(dataManager);

        setTitle("당신을 위한 고효율 학습 몰입 도우미, MODO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createNorthContainer(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContent.setBackground(new Color(245, 245, 245));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));

        JPanel timerArea = createModernPanel("POMODORO TIMER");
        timerArea.add(new TimerPanel(pomodoroService, this::refreshStats), BorderLayout.CENTER);

        JPanel rightArea = new JPanel(new GridLayout(2, 1, 0, 20));
        rightArea.setOpaque(false);

        JPanel todoArea = createModernPanel("TODO-LIST");
        todoArea.add(new TodoPanel(todoService), BorderLayout.CENTER);

        JPanel calendarArea = createModernPanel("CALENDAR & MEMO");
        calendarArea.add(new CalendarMemoPanel(), BorderLayout.CENTER);

        rightArea.add(todoArea);
        rightArea.add(calendarArea);
        mainContent.add(timerArea);
        mainContent.add(rightArea);
        add(mainContent, BorderLayout.CENTER);

        refreshStats();
    }

    private JPanel createNorthContainer() {
        JPanel linkBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        linkBar.setBackground(new Color(180, 32, 41));
        linkBar.setPreferredSize(new Dimension(1280, 40));

        JButton btnStatsLink = createFlatButton("학습 통계", Color.WHITE);
        JButton btnKNUCSE = createFlatButton("컴퓨터학부 공지사항", Color.WHITE);
        JButton btnLMS = createFlatButton("LMS 바로가기", Color.WHITE);
        JButton btnGemini = createFlatButton("Gemini", Color.WHITE);
        JButton btnDict = createFlatButton("어학사전", Color.WHITE);

        btnStatsLink.addActionListener(e -> showStatsDialog());
        btnKNUCSE.addActionListener(e -> openWebPage("https://cse.knu.ac.kr/bbs/board.php?bo_table=sub5_1&lang=kor"));
        btnLMS.addActionListener(e -> openWebPage("https://lms.knu.ac.kr"));
        btnGemini.addActionListener(e -> openWebPage("https://gemini.google.com"));
        btnDict.addActionListener(e -> openWebPage("https://dict.naver.com"));

        linkBar.add(btnStatsLink);
        linkBar.add(btnKNUCSE);
        linkBar.add(btnLMS);
        linkBar.add(btnGemini);
        linkBar.add(btnDict);

        JPanel infoBar = new JPanel(new GridBagLayout());
        infoBar.setBackground(Color.WHITE);
        infoBar.setPreferredSize(new Dimension(1280, 80));
        infoBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 30, 0, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.weightx = 0.2;
        JLabel lblTodayDate = new JLabel(new SimpleDateFormat("yyyy.MM.dd(E)").format(new Date()));
        lblTodayDate.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        infoBar.add(lblTodayDate, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        JPanel goalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        goalPanel.setOpaque(false);
        lblDailyGoal = new JLabel("오늘의 목표: (아직 목표가 설정되지 않았습니다.)");
        lblDailyGoal.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        JButton btnEditGoal = new JButton("수정");
        btnEditGoal.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnEditGoal.setFocusPainted(false);
        btnEditGoal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEditGoal.addActionListener(e -> editDailyGoal());

        goalPanel.add(lblDailyGoal);
        goalPanel.add(btnEditGoal);
        infoBar.add(goalPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        JPanel statsPanel = new JPanel(new GridLayout(2, 1));
        statsPanel.setOpaque(false);
        lblTotalTime = new JLabel("", SwingConstants.RIGHT);
        lblTotalTime.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        lblPomodoroCount = new JLabel("", SwingConstants.RIGHT);
        lblPomodoroCount.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        statsPanel.add(lblTotalTime);
        statsPanel.add(lblPomodoroCount);
        infoBar.add(statsPanel, gbc);

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(linkBar, BorderLayout.NORTH);
        northContainer.add(infoBar, BorderLayout.CENTER);
        return northContainer;
    }

    private JButton createFlatButton(String text, Color textColor) {
        JButton button = new JButton(text);
        button.setForeground(textColor);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setText("<html><u>" + text + "</u></html>");
            }

            public void mouseExited(MouseEvent e) {
                button.setText(text);
            }
        });
        return button;
    }

    private JPanel createModernPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        label.setForeground(Color.GRAY);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private void editDailyGoal() {
        String input = JOptionPane.showInputDialog(this, "오늘 달성할 목표를 입력하세요:");
        if (input != null && !input.trim().isEmpty()) {
            lblDailyGoal.setText("오늘의 목표: " + input.trim());
        }
    }

    private void showStatsDialog() {
        PomodoroStats stats = pomodoroService.getTodayStats();
        JOptionPane.showMessageDialog(this,
                "오늘 완료한 뽀모도로: " + stats.getCompletedPomodoros() + "회\n"
                        + "오늘 총 공부시간: " + stats.getTotalFocusMinutes() + "분");
    }

    private void refreshStats() {
        PomodoroStats stats = pomodoroService.getTodayStats();
        lblTotalTime.setText("총 공부시간  " + stats.getTotalFocusMinutes() + "분");
        lblPomodoroCount.setText("완료한 뽀모도로  " + stats.getCompletedPomodoros() + "회");
    }

    private void openWebPage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "링크를 열 수 없습니다.");
        }
    }
}
