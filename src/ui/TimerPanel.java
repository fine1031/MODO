package ui;

import service.PomodoroService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

public class TimerPanel extends JPanel {
    private enum TimerMode { FOCUS, BREAK }

    private static final Color FOCUS_COLOR = new Color(180, 32, 41);
    private static final Color BREAK_COLOR = new Color(34, 197, 94);

    private final PomodoroService pomodoroService;
    private final Runnable onStatsChanged;
    private final JLabel lblTimeDisplay;
    private final JButton btn25;
    private final JButton btn50;
    private final JButton btn90;
    private final JButton btnTest;
    private final JButton btnToggle;
    private final Timer swingTimer;
    private final JPanel topPanel;
    private final JPanel controlPanel;

    private TimerMode currentMode = TimerMode.FOCUS;
    private int focusMinutes = 25;
    private int maxSeconds = focusMinutes * 60;
    private int breakSeconds = 5 * 60;
    private int remainingSeconds = maxSeconds;
    private double progress = 1.0;
    private boolean focusSessionStarted = false;

    public TimerPanel(PomodoroService pomodoroService, Runnable onStatsChanged) {
        this.pomodoroService = pomodoroService;
        this.onStatsChanged = onStatsChanged;
        this.swingTimer = new Timer(1000, e -> tick());

        setBorder(new EmptyBorder(10, 40, 10, 40));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 10));

        topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        topPanel.setBackground(Color.WHITE);

        btn25 = createStyledButton("25분", new Color(241, 245, 249), new Color(51, 65, 85), new Dimension(100, 40));
        btn50 = createStyledButton("50분", new Color(241, 245, 249), new Color(51, 65, 85), new Dimension(100, 40));
        btn90 = createStyledButton("90분", new Color(241, 245, 249), new Color(51, 65, 85), new Dimension(100, 40));
        btnTest = createStyledButton("1분(test)", new Color(220, 252, 231), new Color(22, 101, 52), new Dimension(110, 40));

        ActionListener timeButtonListener = e -> {
            if (swingTimer.isRunning() || currentMode == TimerMode.BREAK) {
                JOptionPane.showMessageDialog(this, "타이머가 작동 중일 때는 시간을 변경할 수 없습니다.");
                return;
            }
            if (e.getSource() == btn25) {
                setTargetTimes(25, 5 * 60);
            } else if (e.getSource() == btn50) {
                setTargetTimes(50, 10 * 60);
            } else if (e.getSource() == btn90) {
                setTargetTimes(90, 15 * 60);
            } else {
                setTargetTimes(1, 5);
            }
        };

        btn25.addActionListener(timeButtonListener);
        btn50.addActionListener(timeButtonListener);
        btn90.addActionListener(timeButtonListener);
        btnTest.addActionListener(timeButtonListener);

        topPanel.add(btn25);
        topPanel.add(btn50);
        topPanel.add(btn90);
        topPanel.add(btnTest);
        add(topPanel, BorderLayout.NORTH);

        lblTimeDisplay = new JLabel("25:00", SwingConstants.CENTER);
        lblTimeDisplay.setFont(new Font("맑은 고딕", Font.BOLD, 85));
        lblTimeDisplay.setForeground(new Color(30, 41, 59));
        lblTimeDisplay.setBorder(new EmptyBorder(0, 0, 40, 0));
        add(lblTimeDisplay, BorderLayout.CENTER);

        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(Color.WHITE);

        btnToggle = createStyledButton("START", FOCUS_COLOR, Color.WHITE, new Dimension(135, 55));
        btnToggle.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        btnToggle.addActionListener(e -> toggleTimer());

        JButton btnReset = createStyledButton("RESET", new Color(226, 232, 240), new Color(71, 85, 105), new Dimension(135, 55));
        btnReset.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        btnReset.addActionListener(e -> {
            if (currentMode == TimerMode.FOCUS && focusSessionStarted) {
                pomodoroService.cancelPomodoro();
                onStatsChanged.run();
            }
            resetTimer();
        });

        controlPanel.add(btnToggle);
        controlPanel.add(btnReset);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void toggleTimer() {
        if (swingTimer.isRunning()) {
            swingTimer.stop();
            btnToggle.setText(currentMode == TimerMode.FOCUS ? "START" : "BREAK");
            btnToggle.setBackground(currentMode == TimerMode.FOCUS ? FOCUS_COLOR : BREAK_COLOR);
            return;
        }

        if (currentMode == TimerMode.FOCUS) {
            focusSessionStarted = true;
        }
        swingTimer.start();
        btnToggle.setText("PAUSE");
        btnToggle.setBackground(new Color(30, 41, 59));
        toggleTimeButtons(false);
    }

    private void tick() {
        remainingSeconds--;
        updateTimeDisplay();

        if (remainingSeconds <= 0) {
            swingTimer.stop();
            if (currentMode == TimerMode.FOCUS) {
                pomodoroService.completePomodoro(focusMinutes);
                focusSessionStarted = false;
                onStatsChanged.run();

                String breakMessage = breakSeconds >= 60
                        ? breakSeconds / 60 + "분"
                        : breakSeconds + "초";
                JOptionPane.showMessageDialog(this, "몰입 완료! " + breakMessage + " 휴식을 시작합니다.");
                enterBreakMode();
            } else {
                JOptionPane.showMessageDialog(this, "휴식 끝! 다시 집중 모드로 돌아갑니다.");
                enterFocusMode();
            }
        }
    }

    private void enterBreakMode() {
        currentMode = TimerMode.BREAK;
        remainingSeconds = breakSeconds;
        progress = 1.0;
        updateUIForMode(new Color(240, 253, 244), BREAK_COLOR, "BREAK");
        swingTimer.start();
    }

    private void enterFocusMode() {
        currentMode = TimerMode.FOCUS;
        remainingSeconds = maxSeconds;
        progress = 1.0;
        focusSessionStarted = false;
        updateUIForMode(Color.WHITE, FOCUS_COLOR, "START");
    }

    private void setTargetTimes(int focusMinutes, int breakSeconds) {
        this.focusMinutes = focusMinutes;
        this.maxSeconds = focusMinutes * 60;
        this.breakSeconds = breakSeconds;
        resetTimer();
    }

    private void resetTimer() {
        swingTimer.stop();
        currentMode = TimerMode.FOCUS;
        remainingSeconds = maxSeconds;
        progress = 1.0;
        focusSessionStarted = false;
        updateUIForMode(Color.WHITE, FOCUS_COLOR, "START");
    }

    private void updateUIForMode(Color background, Color accent, String buttonText) {
        setBackground(background);
        topPanel.setBackground(background);
        controlPanel.setBackground(background);
        lblTimeDisplay.setForeground(currentMode == TimerMode.FOCUS ? new Color(30, 41, 59) : accent);
        btnToggle.setBackground(accent);
        btnToggle.setText(buttonText);
        toggleTimeButtons(currentMode == TimerMode.FOCUS);
        updateTimeDisplay();
    }

    private void updateTimeDisplay() {
        int minutes = Math.max(remainingSeconds, 0) / 60;
        int seconds = Math.max(remainingSeconds, 0) % 60;
        lblTimeDisplay.setText(String.format("%02d:%02d", minutes, seconds));
        int totalSeconds = currentMode == TimerMode.FOCUS ? maxSeconds : breakSeconds;
        progress = (double) Math.max(remainingSeconds, 0) / totalSeconds;
        repaint();
    }

    private void toggleTimeButtons(boolean enabled) {
        btn25.setEnabled(enabled);
        btn50.setEnabled(enabled);
        btn90.setEnabled(enabled);
        btnTest.setEnabled(enabled);
    }

    private JButton createStyledButton(String text, Color background, Color foreground, Dimension size) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setPreferredSize(size);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 - 20;
        int radius = 145;

        g2.setStroke(new BasicStroke(6));
        g2.setColor(new Color(226, 232, 240));
        g2.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        g2.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(currentMode == TimerMode.FOCUS ? FOCUS_COLOR : BREAK_COLOR);
        g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, (int) (-360 * progress));
    }
}
