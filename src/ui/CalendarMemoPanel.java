package ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class CalendarMemoPanel extends JPanel {
    public CalendarMemoPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);

        JLabel label = new JLabel("간단 메모");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JTextArea memoArea = new JTextArea();
        memoArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        memoArea.setLineWrap(true);
        memoArea.setWrapStyleWord(true);

        add(label, BorderLayout.NORTH);
        add(new JScrollPane(memoArea), BorderLayout.CENTER);
    }
}
