# SPEC-03: 타이머 휴식 모드

**왜:** 포모도로 기법의 핵심은 집중 후 짧은 휴식입니다. 집중 세션 완료 시 5분 휴식 타이머로 자동 전환되어야 합니다.

---

## 건드릴 파일

- `src/ui/TimerPanel.java` — 모드 전환 로직 + 배경색 변경

> **이 파일 외에는 건드리지 마세요.**  
> `PomodoroService`, `DataManager`는 변경 없음. 휴식 시간은 기록하지 않습니다.

---

## 구현 가이드

### 1. TimerPanel에 모드 상태 추가

```java
private enum TimerMode { FOCUS, BREAK }
private TimerMode currentMode = TimerMode.FOCUS;
private static final int BREAK_SECONDS = 5 * 60; // 5분 휴식
private static final Color BREAK_COLOR = new Color(34, 197, 94);   // 초록
private static final Color FOCUS_COLOR = new Color(180, 32, 41);   // 빨강 (MODO_RED)
```

### 2. tick() 수정 — 집중 완료 시 휴식 모드 진입

```java
private void tick() {
    remainingSeconds--;
    updateTimeDisplay();

    if (remainingSeconds <= 0) {
        swingTimer.stop();

        if (currentMode == TimerMode.FOCUS) {
            // 집중 완료: 기록 저장 후 휴식 모드 전환
            pomodoroService.completePomodoro(focusMinutes);
            onStatsChanged.run();
            JOptionPane.showMessageDialog(this, "집중 완료! 5분 휴식을 시작합니다.");
            enterBreakMode();
        } else {
            // 휴식 완료: 집중 모드로 복귀
            JOptionPane.showMessageDialog(this, "휴식 완료! 다시 집중해 봅시다.");
            enterFocusMode();
        }
    }
}
```

### 3. enterBreakMode() / enterFocusMode() 추가

```java
private void enterBreakMode() {
    currentMode = TimerMode.BREAK;
    remainingSeconds = BREAK_SECONDS;
    progress = 1.0;
    btnToggle.setText("BREAK");
    btnToggle.setBackground(BREAK_COLOR);
    setBackground(new Color(240, 253, 244)); // 연한 초록 배경
    lblTimeDisplay.setForeground(BREAK_COLOR);
    toggleTimeButtons(false);
    updateTimeDisplay();
    swingTimer.start();
}

private void enterFocusMode() {
    currentMode = TimerMode.FOCUS;
    remainingSeconds = maxSeconds;
    progress = 1.0;
    btnToggle.setText("START");
    btnToggle.setBackground(FOCUS_COLOR);
    setBackground(Color.WHITE);
    lblTimeDisplay.setForeground(new Color(30, 41, 59));
    toggleTimeButtons(true);
    updateTimeDisplay();
}
```

### 4. paintComponent() — 원형 진행 색상 분기

```java
g2.setColor(currentMode == TimerMode.FOCUS ? FOCUS_COLOR : BREAK_COLOR);
g2.drawArc(...);
```

### 5. resetTimer() 에 모드 초기화 추가

```java
private void resetTimer() {
    swingTimer.stop();
    currentMode = TimerMode.FOCUS; // 항상 집중 모드로 초기화
    remainingSeconds = maxSeconds;
    progress = 1.0;
    setBackground(Color.WHITE);
    lblTimeDisplay.setForeground(new Color(30, 41, 59));
    btnToggle.setText("START");
    btnToggle.setBackground(FOCUS_COLOR);
    toggleTimeButtons(true);
    updateTimeDisplay();
}
```

---

## 성공 기준

- [ ] 집중 타이머가 0:00이 되면 팝업 후 5:00 휴식 타이머가 자동 시작된다.
- [ ] 휴식 모드 중 배경색이 초록 계열로 바뀐다.
- [ ] 휴식 타이머가 끝나면 팝업 후 집중 모드(선택된 시간)로 복귀한다.
- [ ] RESET 버튼을 누르면 항상 집중 모드 시작 상태로 돌아간다.
- [ ] 휴식 중 시간 버튼(25/50/90분)이 비활성화된다.

---

## 의존성

- `TimerPanel.java` 단독 수정. 다른 팀원 작업과 충돌 없음.

---

## 함정

- **배경색 복구:** `setBackground()`로 색을 바꾸면 반드시 모드 전환 시 원래 색으로 되돌려야 합니다. `resetTimer()`에서 `Color.WHITE`로 복구를 빠뜨리면 버그가 생깁니다.
- **연속 팝업 문제:** 타이머가 멈추지 않은 상태에서 팝업이 뜨면 팝업이 닫히는 동안 `tick()`이 계속 호출될 수 있습니다. `swingTimer.stop()`은 반드시 팝업 호출 전에 실행해야 합니다 (위 코드 순서 주의).
- **색상 repaint:** `setBackground()` 후 `repaint()`를 명시적으로 호출해야 원형 진행 바 색상도 함께 바뀝니다.
