# MODO

Java Swing 기반 학습 몰입 도우미 프로젝트입니다. 사용자가 오늘 할 일을 정리하고, 뽀모도로 타이머로 집중 시간을 기록한 뒤, 오늘의 총 공부 시간과 완료 횟수를 확인할 수 있게 만드는 것이 1차 MVP 목표입니다.

## 실행 방법

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out Main
```

## 현재 프로젝트 구조

```text
src/
├── Main.java
├── model/
│   ├── PomodoroStats.java
│   └── Todo.java
├── service/
│   ├── PomodoroService.java
│   └── TodoService.java
├── storage/
│   └── DataManager.java
└── ui/
    ├── CalendarMemoPanel.java
    ├── MainFrame.java
    ├── TimerPanel.java
    └── TodoPanel.java
```

## 클래스 책임

- `Main.java`: 프로그램 시작점, Swing UI 실행
- `ui.MainFrame`: 전체 레이아웃, 상단 링크바, 오늘 목표/통계 표시
- `ui.TimerPanel`: 타이머 UI, 시작/일시정지/리셋, 완료 처리 호출
- `ui.TodoPanel`: TODO 입력/추가/완료/삭제 UI
- `ui.CalendarMemoPanel`: 캘린더/메모 영역 placeholder
- `model.Todo`: 할 일 데이터
- `model.PomodoroStats`: 오늘의 뽀모도로 기록
- `service.TodoService`: TODO 추가/삭제/완료 처리
- `service.PomodoroService`: 뽀모도로 완료 기록 처리
- `storage.DataManager`: 파일 저장/불러오기

## 저장 파일

앱 실행 후 데이터는 `data/` 폴더에 저장됩니다.

- `data/todo_list.txt`: `title|completed|targetDate`
- `data/pomodoro_stats.txt`: `date|completedPomodoros|totalFocusMinutes`

## 브랜치 규칙

- `main` 브랜치에 직접 push하지 않습니다.
- 기능별 브랜치에서 작업한 뒤 Pull Request로 합칩니다.
- PR은 최소 1명이 확인한 뒤 merge합니다.

브랜치 예시:

- `feature/main-ui`
- `feature/todo`
- `feature/timer`
- `feature/stats`
- `feature/calendar-memo`
- `fix/ui-layout`

## 커밋 메시지 예시

- `feat: 메인 화면 레이아웃 구현`
- `feat: TODO 추가 기능 구현`
- `feat: 포모도로 타이머 시작 기능 구현`
- `fix: 타이머 리셋 오류 수정`
- `refactor: DataManager 저장 로직 분리`
- `docs: 프로젝트 README 작성`

## 팀원별 추천 작업

- 지헌님: `ui/MainFrame.java`, 전체 UI 레이아웃, 상단 링크바/정보바 정리
- 태욱님: `ui/TodoPanel.java`, `service/TodoService.java`, `model/Todo.java`
- 타이머 담당자: `ui/TimerPanel.java`, `service/PomodoroService.java`, `model/PomodoroStats.java`
- 통합 담당자: GitHub repo 관리, PR 확인, README/실행 방법 관리

## 다음 이슈 단위 작업

1. TODO 수정 기능 추가
   - 완료 기준: 기존 TODO 제목을 수정할 수 있고, 앱 재실행 후 수정된 제목이 유지된다.

2. 오늘 목표 저장 기능 추가
   - 완료 기준: 오늘의 목표를 입력하면 앱 재실행 후에도 같은 날짜의 목표가 표시된다.

3. 타이머 휴식 모드 추가
   - 완료 기준: 집중 시간이 끝나면 5분 휴식 타이머로 전환된다.

4. 통계 표시 개선
   - 완료 기준: 오늘 총 공부 시간이 `분`뿐 아니라 `HH:MM` 형식으로도 확인된다.

5. 메모 저장 기능 추가
   - 완료 기준: 메모 입력 후 앱 재실행 시 메모 내용이 유지된다.

6. 캘린더 영역 구현
   - 완료 기준: 오늘 날짜가 표시되고, 날짜별 메모/기록 확장 위치가 분리된다.

## 1차 MVP 완료 기준

- `Main.java` 실행 시 `MainFrame`이 정상적으로 열린다.
- `MainFrame`이 `TimerPanel`, `TodoPanel`, `CalendarMemoPanel`을 포함한다.
- `TodoPanel`에서 할 일 추가/삭제/완료가 가능하다.
- `TimerPanel`에서 타이머 시작/일시정지/리셋이 가능하다.
- 타이머 완료 시 오늘 공부 기록이 증가한다.
- 앱 재실행 후 TODO와 오늘 공부 기록을 불러올 수 있다.
