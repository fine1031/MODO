# SPEC-07: 통계 패널 구현 (StatsPanel + 성공률 추적)

**왜:** PRD Phase 1 필수 항목인 JTable 통계 뷰가 없고, 성공률 계산에 필요한 취소 횟수가 추적되지 않습니다.

---

## 건드릴 파일

| 파일 | 변경 내용 |
|------|----------|
| `src/model/PomodoroStats.java` | `cancelledPomodoros` 필드 추가 |
| `src/storage/DataManager.java` | 파일 포맷 컬럼 추가 |
| `src/service/PomodoroService.java` | `cancelPomodoro()` 메서드 추가 |
| `src/ui/TimerPanel.java` | RESET 시 취소 처리 호출 |
| `src/ui/StatsPanel.java` | **신규 파일** — JTable 통계 뷰 |
| `src/ui/MainFrame.java` | 오른쪽 하단을 JTabbedPane으로 교체 |

> `src/ui/CalendarMemoPanel.java`, `TodoPanel.java`, `TodoService.java` 건드리지 마세요.

---

## 변경된 저장 파일 형식

`data/pomodoro_stats.txt` 포맷에 `cancelledPomodoros` 컬럼이 추가됩니다.

**기존:**
```
date|completedPomodoros|totalFocusMinutes
2026-05-24|4|100
```

**변경 후:**
```
date|completedPomodoros|cancelledPomodoros|totalFocusMinutes
2026-05-24|4|2|100
```

> **주의:** 기존 `data/pomodoro_stats.txt` 파일이 있으면 삭제 후 재실행하세요. 컬럼 수가 달라 파싱 오류가 납니다.

---

## 구현 가이드

### 1. PomodoroStats — 필드 추가

```java
public class PomodoroStats {
    private String date;
    private int completedPomodoros;
    private int cancelledPomodoros; // 추가
    private int totalFocusMinutes;

    public PomodoroStats(String date, int completedPomodoros, int cancelledPomodoros, int totalFocusMinutes) {
        this.date = date;
        this.completedPomodoros = completedPomodoros;
        this.cancelledPomodoros = cancelledPomodoros;
        this.totalFocusMinutes = totalFocusMinutes;
    }

    public int getCancelledPomodoros() { return cancelledPomodoros; }

    public void cancelPomodoro() { cancelledPomodoros++; }

    public double getSuccessRate() {
        int total = completedPomodoros + cancelledPomodoros;
        if (total == 0) return 0.0;
        return (double) completedPomodoros / total * 100;
    }

    // 기존 addFocusSession(), getDate(), getCompletedPomodoros(), getTotalFocusMinutes() 유지
}
```

### 2. DataManager — 포맷 변경

`loadTodayStats()` 와 `loadAllStats()` (SPEC-06에서 추가된 메서드) 파싱 수정:

```java
// loadTodayStats()
if (parts.length >= 4 && today.equals(parts[0])) {
    return new PomodoroStats(
        parts[0],
        Integer.parseInt(parts[1]),
        Integer.parseInt(parts[2]), // cancelledPomodoros
        Integer.parseInt(parts[3])  // totalFocusMinutes
    );
}
// 파일 없거나 날짜 다르면
return new PomodoroStats(today, 0, 0, 0);

// saveStats() — 직렬화에 cancelledPomodoros 추가
writer.write(s.getDate() + "|" + s.getCompletedPomodoros()
    + "|" + s.getCancelledPomodoros()
    + "|" + s.getTotalFocusMinutes());
```

### 3. PomodoroService — cancelPomodoro() 추가

```java
public void cancelPomodoro() {
    todayStats.cancelPomodoro();
    dataManager.saveStats(todayStats);
}
```

### 4. TimerPanel — RESET 시 취소 처리

현재 `resetTimer()`는 타이머 완료 후 자동 호출과 사용자 RESET 버튼 클릭을 구분하지 않아요.
버튼 클릭 시에만 취소로 기록해야 합니다.

```java
// RESET 버튼 리스너 (생성자에서)
JButton btnReset = createStyledButton("RESET", ...);
btnReset.addActionListener(e -> {
    if (swingTimer.isRunning()) {
        // 타이머가 돌던 중 취소 → 취소 횟수 증가
        pomodoroService.cancelPomodoro();
        onStatsChanged.run();
    }
    resetTimer();
});

// tick()에서 완료 시 resetTimer() 호출은 그대로 유지 (취소 아님)
```

### 5. StatsPanel.java — 신규 파일

```java
package ui;

import model.PomodoroStats;
import service.PomodoroService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

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
        btnRefresh.addActionListener(e -> refreshTable());
        btnRefresh.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnRefresh, BorderLayout.SOUTH);
        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0); // 기존 행 초기화
        PomodoroStats stats = pomodoroService.getTodayStats();

        int h = stats.getTotalFocusMinutes() / 60;
        int m = stats.getTotalFocusMinutes() % 60;
        String timeStr = (h > 0 ? h + "시간 " : "") + m + "분";

        tableModel.addRow(new Object[]{"오늘 총 집중 시간", timeStr});
        tableModel.addRow(new Object[]{"완료한 뽀모도로", stats.getCompletedPomodoros() + "회"});
        tableModel.addRow(new Object[]{"취소한 뽀모도로", stats.getCancelledPomodoros() + "회"});
        tableModel.addRow(new Object[]{"성공률", String.format("%.1f%%", stats.getSuccessRate())});
    }
}
```

### 6. MainFrame — JTabbedPane으로 교체

```java
// 기존 (변경 전)
JPanel calendarArea = createModernPanel("CALENDAR & MEMO");
calendarArea.add(new CalendarMemoPanel(), BorderLayout.CENTER);

// 변경 후
StatsPanel statsPanel = new StatsPanel(pomodoroService);
JTabbedPane tabbedPane = new JTabbedPane();
tabbedPane.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
tabbedPane.addTab("캘린더 & 메모", new CalendarMemoPanel(dataManager)); // SPEC-05 완료 후
tabbedPane.addTab("학습 통계", statsPanel);

JPanel calendarArea = createModernPanel("CALENDAR & STATS");
calendarArea.add(tabbedPane, BorderLayout.CENTER);
```

`refreshStats()` 메서드에 StatsPanel 갱신 추가:

```java
// StatsPanel을 필드로 선언
private StatsPanel statsPanel;

private void refreshStats() {
    PomodoroStats stats = pomodoroService.getTodayStats();
    int totalMinutes = stats.getTotalFocusMinutes();
    lblTotalTime.setText("총 공부시간  " + formatMinutes(totalMinutes));
    lblPomodoroCount.setText("완료한 뽀모도로  " + stats.getCompletedPomodoros() + "회");
    statsPanel.refreshTable(); // StatsPanel도 함께 갱신
}
```

---

## 성공 기준

- [ ] 오른쪽 하단에 "캘린더 & 메모" / "학습 통계" 탭이 표시된다.
- [ ] "학습 통계" 탭에 JTable로 총 집중 시간, 완료 횟수, 취소 횟수, 성공률이 표시된다.
- [ ] 타이머 완료 시 InfoBar와 통계 탭이 동시에 갱신된다.
- [ ] 타이머 진행 중 RESET 클릭 시 취소 횟수가 1 증가한다.
- [ ] 타이머가 0:00에 자동 완료된 경우 취소 횟수는 증가하지 않는다.
- [ ] 성공률 = `완료 / (완료 + 취소) × 100` 이 소수점 1자리로 표시된다.
- [ ] 완료/취소가 모두 0일 때 성공률이 `0.0%`로 표시되고 오류가 없다.

---

## 의존성

- **SPEC-02 의존:** `MainFrame`에서 `dataManager` 필드 사용.
- **SPEC-05 의존:** `CalendarMemoPanel(dataManager)` 생성자 형태 사용.
- **SPEC-06 부분 의존:** `DataManager.saveStats()`가 누적 저장 방식으로 바뀐 경우 포맷 충돌 주의. SPEC-06과 동시에 진행하면 `saveStats()` 수정이 겹칩니다 — 한 명이 담당.
- `PomodoroStats` 생성자가 바뀌므로 `DataManager.loadTodayStats()`의 `new PomodoroStats(...)` 호출도 반드시 함께 수정.

---

## 함정

- **생성자 변경 파급:** `PomodoroStats(String, int, int, int)`로 바꾸면 `DataManager`에서 호출하는 모든 `new PomodoroStats(...)` 코드를 찾아서 다 수정해야 합니다. 컴파일 오류로 바로 잡힘.
- **RESET과 완료 구분:** `resetTimer()`를 RESET 버튼 리스너와 `tick()` 내부 두 곳에서 호출하는데, 취소 카운트는 RESET 버튼에서만 해야 합니다. `tick()` 내부의 `resetTimer()` 호출 앞에 `cancelPomodoro()`가 들어가면 완료도 취소로 잘못 기록됩니다.
- **기존 stats 파일 삭제 필수:** 컬럼이 `3→4`개로 바뀌므로 기존 파일을 그대로 두면 `Integer.parseInt(parts[3])`에서 `ArrayIndexOutOfBoundsException` 발생.
