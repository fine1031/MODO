# SPEC-04: 통계 표시 개선 (HH:MM 형식)

**왜:** 현재 총 공부시간이 "100분"처럼 분 단위로만 표시됩니다. "1시간 40분" 또는 "01:40" 형식으로 보여주면 가독성이 올라갑니다.

---

## 건드릴 파일

- `src/ui/MainFrame.java` — `refreshStats()` 메서드만 수정

> **이 파일 외에는 건드리지 마세요.**  
> `PomodoroStats`, `PomodoroService`, `DataManager` 변경 없음.

---

## 구현 가이드

### refreshStats() 수정

```java
private void refreshStats() {
    PomodoroStats stats = pomodoroService.getTodayStats();
    int totalMinutes = stats.getTotalFocusMinutes();

    String timeText = formatMinutes(totalMinutes);
    lblTotalTime.setText("총 공부시간  " + timeText);
    lblPomodoroCount.setText("완료한 뽀모도로  " + stats.getCompletedPomodoros() + "회");
}

private String formatMinutes(int totalMinutes) {
    if (totalMinutes == 0) {
        return "0분";
    }
    int hours = totalMinutes / 60;
    int minutes = totalMinutes % 60;
    if (hours == 0) {
        return minutes + "분";
    }
    if (minutes == 0) {
        return hours + "시간";
    }
    return hours + "시간 " + minutes + "분";
}
```

### showStatsDialog()도 동일하게 수정

```java
private void showStatsDialog() {
    PomodoroStats stats = pomodoroService.getTodayStats();
    JOptionPane.showMessageDialog(this,
            "오늘 완료한 뽀모도로: " + stats.getCompletedPomodoros() + "회\n"
                    + "오늘 총 공부시간: " + formatMinutes(stats.getTotalFocusMinutes()));
}
```

---

## 성공 기준

- [ ] 총 공부시간 0분 → "0분" 표시.
- [ ] 총 공부시간 25분 → "25분" 표시.
- [ ] 총 공부시간 60분 → "1시간" 표시.
- [ ] 총 공부시간 90분 → "1시간 30분" 표시.
- [ ] 상단 정보바와 통계 다이얼로그 두 곳 모두 동일 형식으로 표시.

---

## 의존성

- `MainFrame.java`만 수정. 팀장(오지헌)과 협업 시 코드 충돌 주의.
- 다른 패널 수정 없음.

---

## 함정

- **private 메서드 위치:** `formatMinutes()`는 `private`으로 `MainFrame` 안에 추가합니다. 별도 유틸 클래스로 만들 필요 없습니다.
- **테스트 방법:** 실제 타이머를 25분 돌리지 않아도 됩니다. `data/pomodoro_stats.txt`를 직접 열어서 `totalFocusMinutes` 값을 `90`으로 수정 후 앱을 재실행하면 바로 확인할 수 있습니다.
