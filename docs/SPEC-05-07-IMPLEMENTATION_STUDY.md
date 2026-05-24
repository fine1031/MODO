# SPEC 05-07 구현 학습 공유 문서

이 문서는 `SPEC-05 메모 저장`, `SPEC-06 캘린더`, `SPEC-07 통계 패널` 구현을 팀원들이 함께 이해하고 발표/코드 리뷰에 대비할 수 있도록 정리한 자료입니다.

## 먼저 볼 파일

아래 순서대로 보면 흐름이 가장 자연스럽습니다.

1. `src/model/PomodoroStats.java`
2. `src/storage/DataManager.java`
3. `src/service/PomodoroService.java`
4. `src/ui/CalendarMemoPanel.java`
5. `src/ui/StatsPanel.java`
6. `src/ui/TimerPanel.java`
7. `src/ui/MainFrame.java`

---

## 전체 구현 흐름

SPEC 5-7은 모두 "공부 기록을 저장하고 화면에 보여주는 기능"으로 연결됩니다.

```text
타이머 완료/취소
  -> TimerPanel
  -> PomodoroService
  -> PomodoroStats 변경
  -> DataManager가 data/pomodoro_stats.txt 저장
  -> MainFrame 상단 통계 + StatsPanel 갱신
  -> CalendarMemoPanel에서 날짜별 기록 조회
```

핵심은 UI가 파일을 직접 수정하지 않는다는 점입니다.

- `TimerPanel`: 버튼 클릭과 화면 갱신 담당
- `PomodoroService`: 완료/취소 같은 기록 변경 담당
- `PomodoroStats`: 하루 통계 데이터 보관
- `DataManager`: 파일 저장/불러오기 담당
- `StatsPanel`, `CalendarMemoPanel`: 저장된 데이터를 보기 좋게 표시

---

## SPEC-05: 메모 저장 기능

### 구현된 내용

`DataManager`에 오늘 메모를 저장하고 불러오는 메서드가 추가되어 있습니다.

- `loadTodayMemo()`
- `saveTodayMemo(String memo)`

저장 파일은 `data/memo.txt`이고 형식은 다음과 같습니다.

```text
2026-05-24|오늘 공부 내용
```

여러 줄 메모는 파일 한 줄에 저장하기 위해 줄바꿈을 치환합니다.

```text
메모 입력값의 \n -> 파일 저장 시 <NL>
파일 로드 시 <NL> -> \n
```

### 현재 확인할 점

현재 코드 기준으로는 `DataManager`의 메모 저장/로드 메서드는 존재하지만, `CalendarMemoPanel` 화면은 달력 중심으로 재작성되어 메모 입력창과 저장 버튼이 보이지 않습니다.

즉, SPEC-05는 저장 로직은 구현되어 있지만 UI 연결은 다시 확인이 필요합니다.

팀원이 발표할 때는 이렇게 말하면 정확합니다.

> 메모 저장 기능은 `DataManager`에 파일 입출력 메서드로 구현되어 있습니다. 다만 이후 SPEC-06에서 `CalendarMemoPanel`이 달력 화면으로 바뀌면서, 메모 입력 UI가 현재 화면에 노출되지 않는 상태라 UI 연결 보완이 필요합니다.

### 공부할 개념

- `Files.newBufferedReader`, `Files.newBufferedWriter`
- `try-with-resources`
- `String.split("\\|", 2)`
- 문자열 치환: `replace("\n", "<NL>")`
- 오늘 날짜 비교: `LocalDate.now().toString()`

### 코드 읽기 포인트

`loadTodayMemo()`는 파일이 없거나 날짜가 오늘이 아니면 빈 문자열을 반환합니다. 그래서 앱 최초 실행이나 날짜가 바뀐 경우에도 오류 없이 동작합니다.

`saveTodayMemo()`는 `ensureDataDir()`를 먼저 호출합니다. `data/` 폴더가 없으면 저장 전에 폴더를 만들어야 하기 때문입니다.

---

## SPEC-06: 캘린더 영역 구현

### 구현된 내용

`CalendarMemoPanel`은 이번 달 달력을 7열 6행 그리드로 보여줍니다.

- 오늘 날짜는 빨간색
- 기록이 있는 날짜는 연분홍색
- 사용자가 클릭한 날짜는 파란색
- 날짜 클릭 시 하단에 해당 날짜의 기록 표시

관련 파일:

- `src/ui/CalendarMemoPanel.java`
- `src/storage/DataManager.java`
- `src/model/PomodoroStats.java`

### 데이터 구조

캘린더는 날짜별 통계를 빠르게 찾기 위해 `Map<String, PomodoroStats>`를 사용합니다.

```java
Map<String, PomodoroStats> statsMap = dataManager.loadAllStats();
```

Key는 날짜 문자열입니다.

```text
"2026-05-24"
```

Value는 해당 날짜의 통계 객체입니다.

```text
PomodoroStats(date, completedPomodoros, cancelledPomodoros, totalFocusMinutes)
```

### 저장 파일 형식

`data/pomodoro_stats.txt`

```text
date|completedPomodoros|cancelledPomodoros|totalFocusMinutes
2026-05-24|3|1|75
```

### 달력 시작 요일 계산

```java
int startDow = ym.atDay(1).getDayOfWeek().getValue() % 7;
```

Java의 `DayOfWeek`는 월요일이 1, 일요일이 7입니다. 우리 달력은 일요일부터 시작하므로 `% 7`을 사용해 일요일을 0으로 바꿉니다.

예시:

```text
월요일 = 1
화요일 = 2
...
토요일 = 6
일요일 = 7 % 7 = 0
```

### 날짜 버튼 색상 흐름

`CalendarMemoPanel`은 날짜별 상태에 따라 버튼 배경색을 다르게 지정합니다.

```text
오늘 날짜: COLOR_TODAY
기록 있는 날짜: COLOR_HAS_STATS
기본 날짜: COLOR_DEFAULT
선택한 날짜: COLOR_SELECTED
```

클릭 시 이전 선택 버튼의 색을 복원하기 위해 아래 상태를 저장합니다.

```java
private JButton lastSelected;
private Color lastSelectedOriginalColor;
```

### 공부할 개념

- `YearMonth`
- `LocalDate`
- `GridLayout(6, 7, 2, 2)`
- `Map`, `HashMap`
- 버튼 이벤트: `addActionListener`
- UI 상태 저장: 마지막 선택 버튼 기억하기

### 발표용 설명

> 캘린더는 `YearMonth.now()`로 이번 달 정보를 구하고, 1일의 요일만큼 빈 칸을 먼저 넣은 뒤 날짜 버튼을 채우는 방식으로 구현했습니다. 날짜별 통계는 `DataManager.loadAllStats()`가 반환하는 `Map`에서 조회하고, 기록이 있는 날짜는 다른 색으로 표시합니다.

---

## SPEC-07: 통계 패널 구현

### 구현된 내용

`StatsPanel`이 새로 추가되어 오늘의 학습 통계를 `JTable`로 보여줍니다.

표시 항목:

- 오늘 총 집중 시간
- 완료한 뽀모도로
- 취소한 뽀모도로
- 성공률

관련 파일:

- `src/model/PomodoroStats.java`
- `src/service/PomodoroService.java`
- `src/storage/DataManager.java`
- `src/ui/StatsPanel.java`
- `src/ui/TimerPanel.java`
- `src/ui/MainFrame.java`

### 성공률 계산

`PomodoroStats`에서 계산합니다.

```java
public double getSuccessRate() {
    int total = completedPomodoros + cancelledPomodoros;
    if (total == 0) return 0.0;
    return (double) completedPomodoros / total * 100;
}
```

완료와 취소가 모두 0이면 나눗셈 오류를 피하기 위해 `0.0`을 반환합니다.

### 완료와 취소의 기준

완료:

```text
타이머가 00:00까지 도달
-> PomodoroService.completePomodoro(focusMinutes)
```

취소:

```text
타이머가 실행 중인 상태에서 RESET 클릭
-> PomodoroService.cancelPomodoro()
```

중요한 점은 자동 완료 후 `resetTimer()`가 호출되어도 취소로 기록하지 않는다는 것입니다. 취소 처리는 RESET 버튼 이벤트 안에만 있습니다.

### MainFrame 연결 방식

오른쪽 하단 영역은 `JTabbedPane`으로 바뀌었습니다.

```text
탭 1: 캘린더
탭 2: 학습 통계
```

`MainFrame.refreshStats()`는 상단 정보바와 `StatsPanel`을 함께 갱신합니다.

```java
if (statsPanel != null) statsPanel.refreshTable();
```

### JTable 구현 포인트

`StatsPanel`은 `DefaultTableModel`을 사용합니다.

```java
tableModel.setRowCount(0);
tableModel.addRow(...);
```

`setRowCount(0)`으로 기존 행을 지운 뒤 최신 통계로 다시 채웁니다. 이렇게 해야 새로고침할 때 같은 행이 계속 누적되지 않습니다.

### 공부할 개념

- `JTable`
- `DefaultTableModel`
- `isCellEditable()` 오버라이드
- `JTabbedPane`
- 콜백 구조: `Runnable onStatsChanged`
- 계산 로직을 모델에 둘지 서비스에 둘지 판단하기

### 발표용 설명

> 통계 패널은 `JTable`과 `DefaultTableModel`로 구현했습니다. 타이머가 완료되거나 취소되면 `PomodoroService`가 `PomodoroStats`를 변경하고 파일에 저장합니다. 이후 `MainFrame`의 `refreshStats()`가 호출되어 상단 통계와 통계 탭이 동시에 갱신됩니다.

---

## 핵심 코드 흐름 따라가기

### 1. 타이머 완료

```text
TimerPanel.tick()
  -> remainingSeconds가 0 이하인지 확인
  -> pomodoroService.completePomodoro(focusMinutes)
  -> onStatsChanged.run()
  -> MainFrame.refreshStats()
  -> StatsPanel.refreshTable()
```

### 2. 타이머 취소

```text
RESET 버튼 클릭
  -> swingTimer.isRunning()이면 취소로 판단
  -> pomodoroService.cancelPomodoro()
  -> onStatsChanged.run()
  -> resetTimer()
```

### 3. 날짜별 통계 저장

```text
PomodoroService.completePomodoro()
  -> PomodoroStats.addFocusSession()
  -> DataManager.saveStats()
  -> 기존 전체 통계 loadAllStats()
  -> 오늘 날짜 통계만 교체
  -> 전체 파일 다시 저장
```

### 4. 캘린더 기록 표시

```text
CalendarMemoPanel 생성
  -> dataManager.loadAllStats()
  -> statsMap에 날짜별 통계 저장
  -> 날짜 버튼 생성
  -> statsMap.containsKey(dateStr)로 기록 여부 판단
  -> 클릭 시 showDayInfo()
```

---

## 팀원이 꼭 공부해야 할 자바/Swing 개념

### Java 기본

- 클래스와 객체
- 생성자
- 필드와 메서드
- 접근 제어자 `private`, `public`
- `final` 필드의 의미
- 문자열 처리: `split`, `replace`, `String.format`

### 컬렉션

- `List`와 `ArrayList`
- `Map`과 `HashMap`
- `Map.get(key)`
- `Map.put(key, value)`
- `Map.containsKey(key)`

### 파일 입출력

- `Path`
- `Files.exists`
- `Files.createDirectories`
- `BufferedReader`
- `BufferedWriter`
- `try-with-resources`
- 텍스트 파일 포맷 설계

### 날짜 API

- `LocalDate.now()`
- `YearMonth.now()`
- `YearMonth.lengthOfMonth()`
- `getDayOfWeek().getValue()`

### Swing

- `JPanel`
- `JButton`
- `JLabel`
- `JTable`
- `JTabbedPane`
- `JScrollPane`
- `BorderLayout`
- `GridLayout`
- `ActionListener`
- `javax.swing.Timer`

---

## 코드 리뷰 예상 질문

### Q1. 왜 통계를 한 줄만 저장하지 않고 날짜별로 여러 줄 저장하나요?

캘린더에서 과거 날짜의 기록을 보여줘야 하기 때문입니다. 한 줄만 저장하면 오늘 기록은 볼 수 있지만, 어제나 그저께 기록은 사라집니다.

### Q2. 왜 `Map<String, PomodoroStats>`를 사용했나요?

날짜 문자열로 해당 날짜의 통계를 바로 찾기 위해서입니다. 리스트로 저장하면 매번 전체를 반복해서 찾아야 하지만, Map은 날짜를 key로 빠르게 조회할 수 있습니다.

### Q3. 왜 `saveStats()`에서 파일 전체를 다시 저장하나요?

현재 데이터 규모가 작고, 수정/삭제/날짜별 갱신을 단순하게 처리하기 위해서입니다. append 방식은 중복 날짜가 생길 수 있어 별도 정리 로직이 필요합니다.

### Q4. 왜 취소 횟수를 `TimerPanel`이 아니라 `PomodoroStats`에 저장하나요?

취소 횟수는 화면 상태가 아니라 학습 기록 데이터입니다. 그래서 모델인 `PomodoroStats`가 값을 가지고, 서비스인 `PomodoroService`가 변경을 담당합니다.

### Q5. 왜 통계 표를 사용자가 수정하지 못하게 했나요?

통계는 타이머 결과로 계산되는 값이므로 사용자가 직접 수정하면 데이터 신뢰성이 깨집니다. 그래서 `isCellEditable()`을 `false`로 오버라이드했습니다.

### Q6. 현재 SPEC-05는 완전히 끝났나요?

저장/로드 메서드는 구현되어 있지만, 현재 화면에는 메모 입력 UI가 보이지 않습니다. 따라서 "저장 로직 구현 완료, UI 연결 보완 필요"로 보는 것이 정확합니다.

---

## 직접 테스트 체크리스트

### SPEC-05

- [ ] `data/memo.txt`가 없어도 앱이 실행되는가?
- [ ] `DataManager.saveTodayMemo()` 호출 시 `data/memo.txt`가 생성되는가?
- [ ] 여러 줄 메모가 `<NL>`로 저장되고 다시 `\n`으로 복원되는가?
- [ ] 현재 UI에서 메모 입력/저장 버튼이 필요한지 확인했는가?

### SPEC-06

- [ ] 달력이 일요일부터 시작하는가?
- [ ] 오늘 날짜가 빨간색으로 표시되는가?
- [ ] 기록이 있는 날짜가 연분홍색으로 표시되는가?
- [ ] 날짜 클릭 시 하단 문구가 바뀌는가?
- [ ] 기록 없는 날짜는 "기록 없음"으로 표시되는가?

### SPEC-07

- [ ] 학습 통계 탭이 보이는가?
- [ ] 타이머 완료 시 완료 횟수와 총 시간이 증가하는가?
- [ ] 실행 중 RESET을 누르면 취소 횟수가 증가하는가?
- [ ] 성공률이 소수점 1자리로 표시되는가?
- [ ] 완료/취소가 0회일 때 성공률이 `0.0%`로 표시되는가?

---

## 다음 보완 작업 제안

1. `CalendarMemoPanel`에 메모 입력/저장 UI를 다시 합치기
   - 현재 달력 중심 UI에 메모 기능이 화면상 노출되지 않습니다.
   - 방법: `JTabbedPane` 안에 "캘린더"와 "메모"를 한 번 더 나누거나, 달력 하단에 작은 메모 영역을 추가합니다.

2. `TimerPanel`의 `5초(테스트)` 버튼 처리 정리
   - 테스트용 버튼은 발표/제출 전 숨기거나 개발 모드에서만 보이게 하는 것이 좋습니다.
   - 현재 5초 완료 시 `focusMinutes = 0`이라 총 집중 시간이 증가하지 않는 점도 의도인지 확인해야 합니다.

3. `README.md`의 설명 최신화
   - 현재 README 일부는 `CalendarMemoPanel`을 placeholder라고 설명합니다.
   - 실제 구현은 캘린더와 통계 탭까지 들어갔으므로 문서 문구를 맞추는 것이 좋습니다.

4. 날짜별 통계 정렬
   - `HashMap`은 저장 순서를 보장하지 않습니다.
   - 파일을 날짜순으로 저장하고 싶다면 `TreeMap` 또는 정렬된 key 리스트를 사용할 수 있습니다.
