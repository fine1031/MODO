# MODO

Java Swing 기반 학습 몰입 도우미 프로젝트입니다. 사용자가 오늘 할 일을 정리하고, 뽀모도로 타이머로 집중 시간을 기록한 뒤, 오늘의 총 공부 시간과 완료 횟수를 확인할 수 있게 만드는 것이 1차 MVP 목표입니다.

## 문서 (docs/)

| 문서 | 내용 |
|------|------|
| [ASSIGNMENTS.md](docs/ASSIGNMENTS.md) | 팀원별 담당 SPEC, 진행 순서, 의존관계 |
| [QNA.md](docs/QNA.md) | 발표/코드리뷰 질의응답 대비 문서 |
| [CLASS_DIAGRAM.md](docs/CLASS_DIAGRAM.md) | 클래스 구조, 레이어 관계, 타이머 완료 시퀀스 |
| [DATA_MODEL.md](docs/DATA_MODEL.md) | 저장 파일 형식, 필드 설명, 확장 방법 |
| [STUDY_GUIDE.md](docs/STUDY_GUIDE.md) | 자바/백엔드 초보를 위한 핵심 개념 설명 |
| [specs/SPEC-01](docs/specs/SPEC-01-todo-edit.md) | TODO 수정 기능 |
| [specs/SPEC-02](docs/specs/SPEC-02-daily-goal-save.md) | 오늘 목표 저장 ✅ |
| [specs/SPEC-03](docs/specs/SPEC-03-timer-break.md) | 타이머 휴식 모드 |
| [specs/SPEC-04](docs/specs/SPEC-04-stats-format.md) | 통계 표시 개선 |
| [specs/SPEC-05](docs/specs/SPEC-05-memo-save.md) | 메모 저장 기능 ✅ |
| [specs/SPEC-06](docs/specs/SPEC-06-calendar.md) | 캘린더 영역 구현 ✅ |
| [specs/SPEC-07](docs/specs/SPEC-07-stats-panel.md) | 통계 패널 + 성공률 추적 ✅ |

> 새 기능을 맡기 전에 해당 SPEC 파일을 먼저 읽어보세요. 건드릴 파일, 성공 기준, 함정이 정리되어 있습니다.

## 빌드 환경

- **JDK:** OpenJDK 24 (`.tool-versions` 기준)
- **빌드 도구:** 없음 (순수 `javac`)
- **외부 라이브러리:** 없음

JDK 설치 확인:
```bash
java -version
```

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

앱 실행 후 데이터는 `data/` 폴더에 저장됩니다. (`data/`는 `.gitignore`에 포함 — Git에 올라가지 않음)

- `data/todo_list.txt`: `title|completed|targetDate`
- `data/pomodoro_stats.txt`: `date|completedPomodoros|totalFocusMinutes`

파일 형식 상세 → [DATA_MODEL.md](docs/DATA_MODEL.md)

## 설계 결정 사항

| 결정 | 이유 |
|------|------|
| 3패널 동시 표시 (CardLayout 미사용) | 타이머·투두·캘린더를 한눈에 보는 것이 UX상 더 직관적이라 판단 |
| `javax.swing.Timer` 사용 | EDT 안전성 확보. raw Thread + `SwingUtilities.invokeLater` 대신 Swing이 이를 자동 처리 |
| DB 없이 txt 파일 저장 | 설치 의존성 없이 실행 가능, 팀 환경에서 파일 직접 확인·수정 가능 |

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
