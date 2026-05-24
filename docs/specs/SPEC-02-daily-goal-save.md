# SPEC-02: 오늘 목표 저장 기능

**왜:** 현재 "오늘의 목표"는 앱을 재실행하면 초기화됩니다. 같은 날 재실행 시 목표가 유지되어야 합니다.

---

## 건드릴 파일

- `src/storage/DataManager.java` — 목표 저장/로드 메서드 추가
- `src/ui/MainFrame.java` — 시작 시 목표 로드, 수정 시 저장 호출

> **이 파일 외에는 건드리지 마세요.**  
> 새 모델/서비스 클래스 추가 불필요. 단순 문자열이므로 DataManager에서 직접 처리합니다.

---

## 저장 파일 형식

`data/daily_goal.txt` 에 저장합니다.

```
date|goalText
```

예시:
```
2026-05-24|오늘 자바 4챕터 끝내기
```

- 날짜가 오늘과 다르면 목표를 비워서 반환합니다 (자동 초기화).

---

## 구현 가이드

### 1. DataManager에 필드와 메서드 추가

```java
private static final Path GOAL_FILE = DATA_DIR.resolve("daily_goal.txt");

public String loadTodayGoal() {
    String today = LocalDate.now().toString();
    if (!Files.exists(GOAL_FILE)) {
        return "";
    }
    try (BufferedReader reader = Files.newBufferedReader(GOAL_FILE, StandardCharsets.UTF_8)) {
        String line = reader.readLine();
        if (line == null) return "";
        String[] parts = line.split("\\|", 2); // 2로 제한 — goalText에 | 포함 가능
        if (parts.length >= 2 && today.equals(parts[0])) {
            return parts[1];
        }
    } catch (IOException e) {
        System.out.println("오늘 목표를 불러오지 못했습니다.");
    }
    return "";
}

public void saveTodayGoal(String goal) {
    ensureDataDir();
    try (BufferedWriter writer = Files.newBufferedWriter(GOAL_FILE, StandardCharsets.UTF_8)) {
        writer.write(LocalDate.now() + "|" + goal.trim());
    } catch (IOException e) {
        System.out.println("오늘 목표를 저장하지 못했습니다.");
    }
}
```

### 2. MainFrame 수정

생성자에서 DataManager를 필드로 승격하고, 목표를 로드합니다.

```java
// 필드 추가
private final DataManager dataManager;

// 생성자에서 로드
dataManager = new DataManager();
String savedGoal = dataManager.loadTodayGoal();
if (!savedGoal.isEmpty()) {
    lblDailyGoal.setText("오늘의 목표: " + savedGoal);
}

// editDailyGoal() 수정
private void editDailyGoal() {
    String input = JOptionPane.showInputDialog(this, "오늘 달성할 목표를 입력하세요:");
    if (input != null && !input.trim().isEmpty()) {
        lblDailyGoal.setText("오늘의 목표: " + input.trim());
        dataManager.saveTodayGoal(input.trim()); // ← 저장 추가
    }
}
```

---

## 성공 기준

- [ ] 목표 입력 후 앱을 종료하고 재실행하면 같은 날짜에 목표가 표시된다.
- [ ] 다음 날 실행하면 목표가 초기화된다 ("아직 목표가 설정되지 않았습니다." 메시지).
- [ ] 목표에 특수문자(`:`, `!`, `?`)가 포함돼도 정상 저장된다.
- [ ] `data/daily_goal.txt`가 없는 상태(첫 실행)에도 오류 없이 앱이 켜진다.

---

## 의존성

- `MainFrame.java`를 수정하므로 팀장(오지헌)과 함께 작업 또는 사전 공유 필요.
- `DataManager`를 필드로 승격하는 과정에서 `TodoService`, `PomodoroService` 생성자에 전달하던 기존 코드 주의.

---

## 함정

- **split 제한값:** `line.split("\\|", 2)` 에서 `2`를 반드시 넣어야 합니다. 목표 텍스트 안에 `|`가 있을 때 제한 없이 split하면 파싱이 깨집니다.
- **DataManager 중복 생성 주의:** 현재 `MainFrame` 생성자에서 `DataManager`를 `TodoService`와 `PomodoroService`에 각각 넘기고 있습니다. 필드로 만들 때 동일 인스턴스를 공유해야 합니다.
