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
    private static final Color MODO_RED = new Color(180, 32, 41);

    private final PomodoroService pomodoroService;
    private final Runnable onStatsChanged;
    private final JLabel lblTimeDisplay;
    private final JButton btn25;
    private final JButton btn50;
    private final JButton btn90;
    private final JButton btnToggle;
    private final Timer swingTimer;

    private int focusMinutes = 25;
    private int maxSeconds = focusMinutes * 60;
    private int remainingSeconds = maxSeconds;
    private double progress = 1.0;

    public TimerPanel(PomodoroService pomodoroService, Runnable onStatsChanged) {
        this.pomodoroService = pomodoroService;
        this.onStatsChanged = onStatsChanged;
        this.swingTimer = new Timer(1000, e -> tick());

        setBorder(new EmptyBorder(10, 40, 10, 40));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        topPanel.setBackground(Color.WHITE);

        btn25 = createStyledButton("25분", new Color(241, 245, 249), new Color(51, 65, 85), new Dimension(100, 40));
        btn50 = createStyledButton("50분", new Color(241, 245, 249), new Color(51, 65, 85), new Dimension(100, 40));
        btn90 = createStyledButton("90분", new Color(241, 245, 249), new Color(51, 65, 85), new Dimension(100, 40));

        ActionListener timeButtonListener = e -> {
            if (swingTimer.isRunning()) {
                JOptionPane.showMessageDialog(this, "타이머가 작동 중일 때는 시간을 변경할 수 없습니다.");
                return;
            }
            if (e.getSource() == btn25) {
                setTargetTime(25);
            } else if (e.getSource() == btn50) {
                setTargetTime(50);
            } else {
                setTargetTime(90);
            }
        };

        btn25.addActionListener(timeButtonListener);
        btn50.addActionListener(timeButtonListener);
        btn90.addActionListener(timeButtonListener);

        topPanel.add(btn25);
        topPanel.add(btn50);
        topPanel.add(btn90);
        add(topPanel, BorderLayout.NORTH);

        lblTimeDisplay = new JLabel("25:00", SwingConstants.CENTER);
        lblTimeDisplay.setFont(new Font("맑은 고딕", Font.BOLD, 85));
        lblTimeDisplay.setForeground(new Color(30, 41, 59));
        lblTimeDisplay.setBorder(new EmptyBorder(0, 0, 40, 0));
        add(lblTimeDisplay, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(Color.WHITE);

        btnToggle = createStyledButton("START", MODO_RED, Color.WHITE, new Dimension(150, 55));
        btnToggle.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        btnToggle.addActionListener(e -> toggleTimer());

        JButton btnReset = createStyledButton("RESET", new Color(226, 232, 240), new Color(71, 85, 105), new Dimension(120, 55));
        btnReset.addActionListener(e -> {
            if (swingTimer.isRunning()) {
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
            btnToggle.setText("START");
            btnToggle.setBackground(MODO_RED);
            return;
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
            pomodoroService.completePomodoro(focusMinutes);
            onStatsChanged.run();
            JOptionPane.showMessageDialog(this, "한 세션 몰입을 완료했습니다. 고생하셨습니다.");
            resetTimer();
        }
    }

    private void setTargetTime(int minutes) {
        focusMinutes = minutes;
        maxSeconds = minutes * 60;
        resetTimer();
    }

    private void resetTimer() {
        swingTimer.stop();
        remainingSeconds = maxSeconds;
        progress = 1.0;
        btnToggle.setText("START");
        btnToggle.setBackground(MODO_RED);
        toggleTimeButtons(true);
        updateTimeDisplay();
    }

    private void updateTimeDisplay() {
        int minutes = Math.max(remainingSeconds, 0) / 60;
        int seconds = Math.max(remainingSeconds, 0) % 60;
        lblTimeDisplay.setText(String.format("%02d:%02d", minutes, seconds));
        progress = (double) Math.max(remainingSeconds, 0) / maxSeconds;
        repaint();
    }

    private void toggleTimeButtons(boolean enabled) {
        btn25.setEnabled(enabled);
        btn50.setEnabled(enabled);
        btn90.setEnabled(enabled);
    }

    private JButton createStyledButton(String text, Color background, Color foreground, Dimension size) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setPreferredSize(size);
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
        g2.setColor(MODO_RED);
        g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, (int) (-360 * progress));
    }
}
