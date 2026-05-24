# SPEC-06: 캘린더 영역 구현

**왜:** 현재 `CalendarMemoPanel`은 단순 메모 텍스트만 있는 스텁입니다. 실제 달력 뷰를 만들어 날짜를 누르면 해당 날짜의 포모도로 기록을 볼 수 있어야 합니다.

> 이 SPEC은 가장 복잡한 작업입니다. SPEC-05(메모 저장)를 먼저 완료한 뒤 진행하세요.

---

## 건드릴 파일

- `src/ui/CalendarMemoPanel.java` — 전면 재작성
- `src/storage/DataManager.java` — 날짜별 통계 다중 저장 로드 메서드 추가
- `src/model/PomodoroStats.java` — 변경 없음

> `src/service/`, `src/model/Todo.java` 건드리지 마세요.

---

## 데이터 저장 형식 변경

현재 `pomodoro_stats.txt`는 오늘 통계 1줄만 저장합니다.  
캘린더 뷰를 위해 **날짜별 누적 저장**으로 변경합니다.

```
2026-05-22|2|50
2026-05-23|4|100
2026-05-24|3|75
```

> **주의:** 이 변경은 `DataManager`의 `loadTodayStats()`와 `saveStats()` 로직도 함께 바뀝니다. 기존 파일이 있다면 삭제 후 재실행하세요.

---

## 구현 가이드

### 1. DataManager — 전체 통계 로드 메서드 추가

```java
public Map<String, PomodoroStats> loadAllStats() {
    Map<String, PomodoroStats> statsMap = new HashMap<>();
    if (!Files.exists(STATS_FILE)) return statsMap;
    try (BufferedReader reader = Files.newBufferedReader(STATS_FILE, StandardCharsets.UTF_8)) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\|", -1);
            if (parts.length >= 3) {
                statsMap.put(parts[0], new PomodoroStats(
                    parts[0],
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
                ));
            }
        }
    } catch (IOException | NumberFormatException e) {
        System.out.println("전체 통계를 불러오지 못했습니다.");
    }
    return statsMap;
}

// saveStats()도 수정 — 기존 줄은 유지하고 오늘 줄만 갱신
public void saveStats(PomodoroStats stats) {
    Map<String, PomodoroStats> all = loadAllStats();
    all.put(stats.getDate(), stats);
    ensureDataDir();
    try (BufferedWriter writer = Files.newBufferedWriter(STATS_FILE, StandardCharsets.UTF_8)) {
        for (PomodoroStats s : all.values()) {
            writer.write(s.getDate() + "|" + s.getCompletedPomodoros() + "|" + s.getTotalFocusMinutes());
            writer.newLine();
        }
    } catch (IOException e) {
        System.out.println("통계를 저장하지 못했습니다.");
    }
}
```

### 2. CalendarMemoPanel — 달력 구현

```java
public class CalendarMemoPanel extends JPanel {
    private final DataManager dataManager;
    private final Map<String, PomodoroStats> statsMap;
    private final JLabel lblSelectedInfo;

    public CalendarMemoPanel(DataManager dataManager) {
        this.dataManager = dataManager;
        this.statsMap = dataManager.loadAllStats();
        setLayout(new BorderLayout(0, 8));
        setBackground(Color.WHITE);

        // 달력 헤더 (요일)
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        JPanel headerPanel = new JPanel(new GridLayout(1, 7));
        headerPanel.setBackground(Color.WHITE);
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            headerPanel.add(lbl);
        }

        // 달력 그리드 (6행 7열)
        JPanel gridPanel = new JPanel(new GridLayout(6, 7, 2, 2));
        gridPanel.setBackground(new Color(245, 245, 245));
        buildCalendarGrid(gridPanel);

        // 선택 날짜 정보
        lblSelectedInfo = new JLabel("날짜를 클릭하면 해당 날의 기록을 볼 수 있어요.", SwingConstants.CENTER);
        lblSelectedInfo.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        lblSelectedInfo.setForeground(Color.GRAY);

        add(headerPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(lblSelectedInfo, BorderLayout.SOUTH);
    }

    private void buildCalendarGrid(JPanel gridPanel) {
        YearMonth ym = YearMonth.now();
        LocalDate firstDay = ym.atDay(1);
        int startDow = firstDay.getDayOfWeek().getValue() % 7; // 일=0
        int daysInMonth = ym.lengthOfMonth();
        String today = LocalDate.now().toString();

        // 빈 칸 채우기 (월 시작 전)
        for (int i = 0; i < startDow; i++) {
            gridPanel.add(new JLabel());
        }

        // 날짜 버튼
        for (int day = 1; day <= daysInMonth; day++) {
            String dateStr = ym.atDay(day).toString(); // "2026-05-24"
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);

            if (dateStr.equals(today)) {
                btn.setBackground(new Color(180, 32, 41));
                btn.setForeground(Color.WHITE);
            } else if (statsMap.containsKey(dateStr)) {
                btn.setBackground(new Color(254, 226, 226)); // 기록 있는 날: 연분홍
            } else {
                btn.setBackground(Color.WHITE);
            }

            final String d = dateStr;
            btn.addActionListener(e -> showDayInfo(d));
            gridPanel.add(btn);
        }
    }

    private void showDayInfo(String dateStr) {
        PomodoroStats stats = statsMap.get(dateStr);
        if (stats == null) {
            lblSelectedInfo.setText(dateStr + " — 기록 없음");
        } else {
            int h = stats.getTotalFocusMinutes() / 60;
            int m = stats.getTotalFocusMinutes() % 60;
            lblSelectedInfo.setText(String.format(
                "%s — 뽀모도로 %d회 | 집중 %d시간 %d분",
                dateStr, stats.getCompletedPomodoros(), h, m
            ));
        }
    }
}
```

필요한 import 추가:
```java
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
```

---

## 성공 기준

- [ ] 이번 달 달력이 7열 6행 형태로 올바른 요일에 맞춰 표시된다.
- [ ] 오늘 날짜 버튼이 빨간 배경으로 강조된다.
- [ ] 포모도로 기록이 있는 날짜 버튼이 연분홍 배경으로 표시된다.
- [ ] 날짜 클릭 시 하단에 "뽀모도로 N회 | 집중 H시간 M분"이 표시된다.
- [ ] 기록 없는 날짜 클릭 시 "기록 없음" 문구가 표시된다.
- [ ] 타이머 완료 후 새 통계가 `pomodoro_stats.txt`에 누적 저장된다.

---

## 의존성

- **SPEC-05 완료 필요** (CalendarMemoPanel 생성자 변경이 겹침).
- **DataManager 수정이 큼:** `saveStats()` 로직이 바뀌므로 기존 오늘 통계가 초기화될 수 있습니다. 팀원 전체에게 공유 후 진행.
- `MainFrame`에서 `CalendarMemoPanel(dataManager)` 생성자 호출 확인.

---

## 함정

- **`DayOfWeek` 인덱스 주의:** Java의 `getDayOfWeek().getValue()`는 월=1, 일=7을 반환합니다. `% 7`로 변환하면 일=0이 됩니다. 달력 시작 요일(일요일)과 일치시키려면 이 계산이 맞아야 합니다. 값이 다르면 달력이 한 열씩 밀립니다.
- **`saveStats()` 성능:** `loadAllStats()` → 수정 → 전체 재저장 방식은 기록이 많아지면 느려질 수 있습니다. 발표 수준에서는 문제 없음.
- **`YearMonth` import:** `java.time.YearMonth`는 명시적으로 import해야 합니다.
