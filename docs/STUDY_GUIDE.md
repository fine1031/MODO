# 학습 가이드 (Study Guide)

자바 초보 또는 백엔드 초보를 위한 가이드입니다.  
이 파일부터 읽고 코드를 열면 이해 속도가 훨씬 빨라집니다.

---

## 추천 학습 순서

```
1단계: 모델 클래스 읽기       → model/Todo.java, model/PomodoroStats.java
2단계: 저장소 코드 읽기        → storage/DataManager.java
3단계: 서비스 로직 읽기        → service/TodoService.java, service/PomodoroService.java
4단계: UI 코드 읽기            → ui/MainFrame.java → ui/TimerPanel.java → ui/TodoPanel.java
5단계: 진입점 확인             → Main.java
```

SPEC-05, SPEC-06, SPEC-07 구현 내용을 집중적으로 공부하려면
[`SPEC-05-07-IMPLEMENTATION_STUDY.md`](SPEC-05-07-IMPLEMENTATION_STUDY.md)를 함께 읽으세요.

---

## 핵심 개념 1: Thread vs Swing Timer

### 이 프로젝트에서 쓰는 것: `javax.swing.Timer`

`TimerPanel.java`를 보면 `Timer swingTimer = new Timer(1000, e -> tick())`가 있어요.

- **Swing Timer란?** 1000ms(1초)마다 `tick()` 메서드를 자동으로 호출하는 알림 시계입니다.
- **왜 raw Thread 대신 Swing Timer를 쓸까요?** Swing(자바 UI 라이브러리)은 **EDT(Event Dispatch Thread)** 라는 단 하나의 스레드에서만 화면을 그려야 합니다. raw Thread에서 UI 컴포넌트를 직접 건드리면 화면이 깨지거나 프로그램이 멈춥니다. Swing Timer는 EDT 위에서 안전하게 동작해요.

### raw Thread로 구현하면 어떻게 될까? (참고용)

```java
// 위험한 코드 예시 — 이렇게 하면 안 됩니다
new Thread(() -> {
    while (remainingSeconds > 0) {
        Thread.sleep(1000);
        remainingSeconds--;
        // 직접 UI 건드리면 안 됨!
        lblTimeDisplay.setText("..."); // ← EDT 위반
    }
}).start();

// 올바른 raw Thread 방식 (참고)
new Thread(() -> {
    while (remainingSeconds > 0) {
        Thread.sleep(1000);
        remainingSeconds--;
        // SwingUtilities.invokeLater로 EDT에 위임해야 함
        SwingUtilities.invokeLater(() -> lblTimeDisplay.setText("..."));
    }
}).start();
```

> **결론:** `Swing Timer`는 내부적으로 스레드를 사용하면서 EDT 안전성을 자동으로 보장합니다. 이 프로젝트에서 `Swing Timer`를 선택한 이유입니다.

---

## 핵심 개념 2: Collection (컬렉션)

### 이 프로젝트에서 쓰는 것: `ArrayList<Todo>`

`TodoService.java`를 보면 `List<Todo> todos = new ArrayList<>(...)`가 있어요.

- **ArrayList란?** 크기가 자동으로 늘어나는 동적 배열입니다. 일반 배열(`Todo[]`)은 처음에 크기를 고정해야 하지만, ArrayList는 `add()`할 때마다 자동으로 공간을 늘려줍니다.

```java
// 배열 — 크기 고정
Todo[] todos = new Todo[10]; // 최대 10개만 담을 수 있음

// ArrayList — 크기 자동 확장
List<Todo> todos = new ArrayList<>();
todos.add(new Todo("할 일 1", false, "2026-05-24")); // 계속 추가 가능
todos.add(new Todo("할 일 2", false, "2026-05-24"));
```

### 주요 메서드 매핑 (코드 → 개념)

| 코드 | 설명 |
|------|------|
| `todos.add(todo)` | 리스트 맨 뒤에 항목 추가 |
| `todos.remove(index)` | 해당 인덱스 항목 제거 |
| `todos.get(index)` | 해당 인덱스 항목 조회 |
| `todos.size()` | 항목 개수 반환 |
| `Collections.unmodifiableList(todos)` | 외부에서 수정 못 하도록 읽기 전용으로 감싸기 |

---

## 핵심 개념 3: File I/O (파일 입출력)

### 이 프로젝트에서 쓰는 것: `BufferedReader` / `BufferedWriter`

`DataManager.java`가 담당합니다.

```java
// 파일 읽기 — loadTodos()
try (BufferedReader reader = Files.newBufferedReader(TODO_FILE, StandardCharsets.UTF_8)) {
    String line;
    while ((line = reader.readLine()) != null) {
        // 한 줄씩 읽어서 | 로 분리 → Todo 객체 생성
        String[] parts = line.split("\\|", -1);
        todos.add(new Todo(parts[0], Boolean.parseBoolean(parts[1]), parts[2]));
    }
}

// 파일 쓰기 — saveTodos()
try (BufferedWriter writer = Files.newBufferedWriter(TODO_FILE, StandardCharsets.UTF_8)) {
    for (Todo todo : todos) {
        writer.write(todo.getTitle() + "|" + todo.isCompleted() + "|" + todo.getTargetDate());
        writer.newLine(); // 줄바꿈
    }
}
```

- **`try-with-resources`란?** `try (...)` 안에서 열린 파일은 블록이 끝나면 자동으로 닫힙니다. 직접 `reader.close()`를 안 해도 되어서 안전합니다.
- **왜 `BufferedReader`를 쓸까요?** 파일을 한 글자씩 읽으면 느립니다. `Buffered`는 한 번에 여러 글자를 메모리에 올려놓고 읽어서 빠릅니다.

---

## 핵심 개념 4: 예외 처리 (Exception Handling)

### 왜 try-catch가 필요할까?

파일을 읽을 때 파일이 아직 없거나, 형식이 깨져 있을 수 있어요. 예외 처리 없이 이런 상황이 생기면 프로그램이 강제 종료됩니다.

```java
// DataManager.loadTodos() 에서의 예외 처리
try (BufferedReader reader = ...) {
    // 파일 읽기
} catch (IOException e) {
    // 파일이 없거나 읽기 실패해도 프로그램이 죽지 않음
    System.out.println("TODO 데이터를 불러오지 못했습니다.");
}
```

### 자주 만나는 예외 종류

| 예외 | 언제 발생하나 |
|------|--------------|
| `IOException` | 파일을 읽거나 쓸 때 문제가 생길 때 |
| `FileNotFoundException` | 지정한 파일이 없을 때 (IOException의 하위 타입) |
| `NumberFormatException` | `"abc"`를 `Integer.parseInt()`로 변환하려 할 때 |
| `ArrayIndexOutOfBoundsException` | 배열/리스트 인덱스 범위를 벗어날 때 |

---

## 핵심 개념 5: 레이어 아키텍처

이 프로젝트는 3계층으로 분리되어 있어요.

```
UI 계층 (ui.*)
    ↕ 서비스 호출
Service 계층 (service.*)
    ↕ 저장/로드 위임
Storage 계층 (storage.*)
    ↕ 파일 읽기/쓰기
데이터 파일 (data/*.txt)
```

**왜 계층을 나눌까요?**  
- `TodoPanel`(화면)이 파일을 직접 읽으면, 파일 형식이 바뀔 때마다 UI 코드도 고쳐야 합니다.
- 계층을 나누면 UI는 `todoService.addTodo("...")` 한 줄만 알면 되고, 파일 형식 변경은 `DataManager`만 고치면 됩니다.

---

## 이 프로젝트에서 공부할 수 있는 것 (체크리스트)

- [ ] **클래스와 객체**: `Todo`, `PomodoroStats` 클래스 읽기
- [ ] **생성자와 메서드**: `TodoService`, `PomodoroService` 흐름 따라가기
- [ ] **인터페이스 활용**: `Runnable`, `ActionListener` 사용 사례 (`TimerPanel`)
- [ ] **컬렉션**: `ArrayList` 추가/삭제/조회 흐름 (`TodoService`)
- [ ] **파일 입출력**: `BufferedReader`/`BufferedWriter`, `Files` API (`DataManager`)
- [ ] **예외 처리**: `try-catch`, `try-with-resources` (`DataManager`)
- [ ] **Swing 이벤트**: `ActionListener`, `SwingUtilities.invokeLater` 개념 (`TimerPanel`)
- [ ] **레이아웃 매니저**: `BorderLayout`, `GridLayout`, `FlowLayout` (`MainFrame`, `TodoPanel`)
- [ ] **익명 함수(람다)**: `e -> tick()`, `() -> refreshStats()` 문법 (`TimerPanel`)

---

## 추가 학습 자료

| 주제 | 자료 |
|------|------|
| Java 기초 | [점프 투 자바](https://wikidocs.net/book/31) |
| Java Collection | [Java ArrayList 공식 문서](https://docs.oracle.com/en/java/docs/api/java.base/java/util/ArrayList.html) |
| Java File I/O | [Baeldung - Java Files](https://www.baeldung.com/java-nio-2-file-api) |
| Java Swing 기초 | [Oracle Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/) |
| Thread 개념 | [점프 투 자바 - 스레드](https://wikidocs.net/230) |
