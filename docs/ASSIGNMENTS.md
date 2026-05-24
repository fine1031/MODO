# 팀원별 작업 분배

> **읽는 법:** 각 SPEC 파일을 열면 건드릴 파일, 구현 코드, 성공 기준, 함정이 전부 적혀 있습니다.
> SPEC 순서대로 진행하고, 완료하면 노션에 코드를 올려주세요.

---

## 오지헌 (팀장) — MainFrame & DataManager

| 순서 | SPEC | 핵심 작업 | 파일 |
|------|------|----------|------|
| 1 | [SPEC-02](specs/SPEC-02-daily-goal-save.md) | 오늘 목표 파일 저장/로드 | `MainFrame.java`, `DataManager.java` |
| 2 | [SPEC-04](specs/SPEC-04-stats-format.md) | 통계 HH:MM 형식 표시 | `MainFrame.java` |

> SPEC-02를 먼저 올려줘야 이태영이 SPEC-05 작업을 시작할 수 있어요.

---

## 김태욱 — 포모도로 타이머 (TimerPanel)

| 순서 | SPEC | 핵심 작업 | 파일 |
|------|------|----------|------|
| 1 | [SPEC-03](specs/SPEC-03-timer-break.md) | 집중 완료 후 5분 휴식 모드 자동 전환 | `TimerPanel.java` |

> 독립 작업. 다른 팀원 완료 기다릴 필요 없이 바로 시작 가능.

---

## 이예나 — 할 일 관리 (TodoPanel)

| 순서 | SPEC | 핵심 작업 | 파일 |
|------|------|----------|------|
| 1 | [SPEC-01](specs/SPEC-01-todo-edit.md) | 할 일 제목 수정 기능 | `TodoPanel.java`, `TodoService.java` |

> 독립 작업. 다른 팀원 완료 기다릴 필요 없이 바로 시작 가능.

---

## 이태영 — 캘린더 & 통계 (CalendarMemoPanel → StatsPanel)

| 순서 | SPEC | 핵심 작업 | 선행 조건 |
|------|------|----------|----------|
| 1 | [SPEC-05](specs/SPEC-05-memo-save.md) | 메모 파일 저장/로드 + 저장 버튼 | 오지헌 SPEC-02 완료 후 |
| 2 | [SPEC-06](specs/SPEC-06-calendar.md) | 달력 GridLayout + 날짜별 기록 뷰 | SPEC-05 완료 후 |
| 3 | [SPEC-07](specs/SPEC-07-stats-panel.md) | JTable 통계 패널 + 성공률 추적 | SPEC-05 완료 후 |

> SPEC-06과 SPEC-07은 순서 무관하게 병렬 진행 가능. 단, 둘 다 SPEC-05가 먼저여야 함.

---

## 전체 진행 순서 (의존 관계)

```
[지금 당장 시작 가능]
  김태욱  → SPEC-03
  이예나  → SPEC-01
  오지헌  → SPEC-02, SPEC-04

[오지헌 SPEC-02 완료 후]
  이태영  → SPEC-05

[이태영 SPEC-05 완료 후]
  이태영  → SPEC-06
  이태영  → SPEC-07
```

---

## 노션 코드 공유 규칙 (복습)

1. 기존 코드는 건드리지 말고 아래에 새 버전 붙여넣기
2. 코드 맨 위에 수정 사항 한 줄 메모
3. 완료 기준(SPEC 파일의 체크리스트)을 직접 확인 후 올리기

---

## 빌드 & 실행

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out Main
```

> `data/pomodoro_stats.txt`가 있는 상태에서 SPEC-07 적용 시 파일 삭제 필요 (컬럼 수 변경).
