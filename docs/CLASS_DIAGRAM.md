# 클래스 다이어그램 (Class Diagram)

> 노션에서 Mermaid가 렌더링 안 되면 아래 텍스트 다이어그램을 대신 사용하세요.

## Mermaid 다이어그램

```mermaid
classDiagram
    class Main {
        +main(args: String[])
    }

    class MainFrame {
        -pomodoroService: PomodoroService
        -lblDailyGoal: JLabel
        -lblTotalTime: JLabel
        -lblPomodoroCount: JLabel
        +refreshStats()
        -editDailyGoal()
        -showStatsDialog()
    }

    class TimerPanel {
        -pomodoroService: PomodoroService
        -onStatsChanged: Runnable
        -swingTimer: Timer
        -remainingSeconds: int
        -focusMinutes: int
        -progress: double
        +toggleTimer()
        +resetTimer()
        -tick()
    }

    class TodoPanel {
        -todoService: TodoService
        -listPanel: JPanel
        -inputField: JTextField
        +refreshTodos()
        -addTodo()
    }

    class CalendarMemoPanel {
        stub: JTextArea 메모 영역만 존재
    }

    class TodoService {
        -dataManager: DataManager
        -todos: List~Todo~
        +getTodos() List~Todo~
        +addTodo(title: String)
        +removeTodo(index: int)
        +toggleTodo(index: int)
        +saveTodos()
    }

    class PomodoroService {
        -dataManager: DataManager
        -todayStats: PomodoroStats
        +getTodayStats() PomodoroStats
        +completePomodoro(focusMinutes: int)
    }

    class DataManager {
        -DATA_DIR: Path
        -TODO_FILE: Path
        -STATS_FILE: Path
        +loadTodos() List~Todo~
        +saveTodos(todos: List~Todo~)
        +loadTodayStats() PomodoroStats
        +saveStats(stats: PomodoroStats)
    }

    class Todo {
        -title: String
        -completed: boolean
        -targetDate: String
        +getTitle() String
        +setTitle(title: String)
        +isCompleted() boolean
        +setCompleted(completed: boolean)
        +getTargetDate() String
    }

    class PomodoroStats {
        -date: String
        -completedPomodoros: int
        -totalFocusMinutes: int
        +getDate() String
        +getCompletedPomodoros() int
        +getTotalFocusMinutes() int
        +addFocusSession(minutes: int)
    }

    Main --> MainFrame : 생성

    MainFrame --> PomodoroService : 통계 조회
    MainFrame --> TimerPanel : 포함
    MainFrame --> TodoPanel : 포함
    MainFrame --> CalendarMemoPanel : 포함

    TimerPanel --> PomodoroService : completePomodoro()
    TodoPanel --> TodoService : CRUD

    TodoService --> DataManager : 저장/로드
    PomodoroService --> DataManager : 저장/로드

    DataManager ..> Todo : 생성/직렬화
    DataManager ..> PomodoroStats : 생성/직렬화

    TodoService --> Todo : 관리
    PomodoroService --> PomodoroStats : 관리
```

---

## 텍스트 다이어그램 (노션 복붙용)

```
[Main]
  └─ creates ──► [MainFrame]
                    ├─ has ──► [TimerPanel] ──► [PomodoroService]
                    ├─ has ──► [TodoPanel]  ──► [TodoService]
                    ├─ has ──► [CalendarMemoPanel]  (stub)
                    └─ uses ─► [PomodoroService]
                                    │
                              [DataManager] ◄── [TodoService]
                                    │
                          저장: data/todo_list.txt
                               data/pomodoro_stats.txt

모델 계층:
[Todo]           title | completed | targetDate
[PomodoroStats]  date | completedPomodoros | totalFocusMinutes
```

---

## 타이머 완료 시퀀스

```
사용자            TimerPanel         PomodoroService        DataManager         MainFrame
  │                  │                     │                     │                  │
  │ PAUSE 클릭       │                     │                     │                  │
  │─────────────────►│                     │                     │                  │
  │                  │ tick() - 1초마다    │                     │                  │
  │                  │ remainingSeconds = 0 │                     │                  │
  │                  │─────────────────────►│                     │                  │
  │                  │     completePomodoro(focusMinutes)         │                  │
  │                  │                     │──────────────────────►│                  │
  │                  │                     │     saveStats(stats)  │                  │
  │                  │                     │◄──────────────────────│                  │
  │                  │◄────────────────────│                     │                  │
  │                  │ onStatsChanged.run() ──────────────────────────────────────►  │
  │                  │                     │                     │   refreshStats()  │
  │ "완료!" 다이얼로그│                     │                     │                  │
  │◄─────────────────│                     │                     │                  │
```

---

## 레이어 역할 요약

| 레이어 | 패키지 | 역할 |
|--------|--------|------|
| UI | `ui.*` | 화면 그리기, 사용자 입력 받기 |
| Service | `service.*` | 비즈니스 로직 (추가/삭제/완료/세션 기록) |
| Storage | `storage.*` | 파일 읽기/쓰기 |
| Model | `model.*` | 데이터 구조 정의 |
