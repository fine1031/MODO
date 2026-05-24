package ui;

import storage.DataManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class CalendarMemoPanel extends JPanel {
    private final JTextArea memoArea;

    public CalendarMemoPanel(DataManager dataManager) {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);

        JLabel label = new JLabel("간단 메모");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        memoArea = new JTextArea();
        memoArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        memoArea.setLineWrap(true);
        memoArea.setWrapStyleWord(true);
        memoArea.setText(dataManager.loadTodayMemo());

        JButton btnSave = new JButton("메모 저장");
        btnSave.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        btnSave.addActionListener(e -> {
            dataManager.saveTodayMemo(memoArea.getText());
            JOptionPane.showMessageDialog(this, "메모가 저장되었습니다.");
        });

        add(label, BorderLayout.NORTH);
        add(new JScrollPane(memoArea), BorderLayout.CENTER);
        add(btnSave, BorderLayout.SOUTH);
    }
}
