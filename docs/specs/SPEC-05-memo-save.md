# SPEC-05: 메모 저장 기능

**왜:** 현재 `CalendarMemoPanel`의 메모는 앱을 끄면 사라집니다. 재실행 후에도 오늘 메모가 유지되어야 합니다.

---

## 건드릴 파일

- `src/storage/DataManager.java` — 메모 저장/로드 메서드 추가
- `src/ui/CalendarMemoPanel.java` — DataManager 주입, 저장 버튼 추가

> **이 파일 외에는 건드리지 마세요.**

---

## 저장 파일 형식

`data/memo.txt` 에 저장합니다.

```
date|memoText
```

예시:
```
2026-05-24|오늘 자바 스윙 공부 잘 됐다. 내일은 File I/O 집중.
```

- 날짜가 오늘과 다르면 빈 문자열을 반환합니다 (자동 초기화).
- 메모 본문에 줄바꿈(\n)이 있으면 `\n` → `<NL>` 로 치환 후 저장, 로드 시 역변환합니다.

---

## 구현 가이드

### 1. DataManager에 메서드 추가

```java
private static final Path MEMO_FILE = DATA_DIR.resolve("memo.txt");

public String loadTodayMemo() {
    String today = LocalDate.now().toString();
    if (!Files.exists(MEMO_FILE)) return "";
    try (BufferedReader reader = Files.newBufferedReader(MEMO_FILE, StandardCharsets.UTF_8)) {
        String line = reader.readLine();
        if (line == null) return "";
        String[] parts = line.split("\\|", 2);
        if (parts.length >= 2 && today.equals(parts[0])) {
            return parts[1].replace("<NL>", "\n");
        }
    } catch (IOException e) {
        System.out.println("메모를 불러오지 못했습니다.");
    }
    return "";
}

public void saveTodayMemo(String memo) {
    ensureDataDir();
    try (BufferedWriter writer = Files.newBufferedWriter(MEMO_FILE, StandardCharsets.UTF_8)) {
        String encoded = memo.replace("\n", "<NL>");
        writer.write(LocalDate.now() + "|" + encoded);
    } catch (IOException e) {
        System.out.println("메모를 저장하지 못했습니다.");
    }
}
```

### 2. CalendarMemoPanel 수정

```java
public class CalendarMemoPanel extends JPanel {
    private final DataManager dataManager;
    private final JTextArea memoArea;

    public CalendarMemoPanel(DataManager dataManager) {
        this.dataManager = dataManager;
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);

        JLabel label = new JLabel("간단 메모");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        memoArea = new JTextArea();
        memoArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        memoArea.setLineWrap(true);
        memoArea.setWrapStyleWord(true);
        memoArea.setText(dataManager.loadTodayMemo()); // 로드

        JButton btnSave = new JButton("메모 저장");
        btnSave.addActionListener(e -> {
            dataManager.saveTodayMemo(memoArea.getText());
            JOptionPane.showMessageDialog(this, "메모가 저장되었습니다.");
        });

        add(label, BorderLayout.NORTH);
        add(new JScrollPane(memoArea), BorderLayout.CENTER);
        add(btnSave, BorderLayout.SOUTH);
    }
}
```

### 3. MainFrame에서 CalendarMemoPanel 생성 시 dataManager 전달

```java
// 기존
calendarArea.add(new CalendarMemoPanel(), BorderLayout.CENTER);

// 수정
calendarArea.add(new CalendarMemoPanel(dataManager), BorderLayout.CENTER);
```

> `dataManager`는 SPEC-02에서 MainFrame 필드로 승격된 것을 사용합니다. SPEC-02와 함께 작업하거나 완료 후 진행하세요.

---

## 성공 기준

- [ ] 메모 입력 후 "메모 저장" 버튼 클릭 시 저장 완료 팝업이 뜬다.
- [ ] 앱 재실행 후 오늘 날짜면 메모 내용이 복원된다.
- [ ] 다음 날 실행하면 메모가 비워진다.
- [ ] 여러 줄 메모(줄바꿈 포함)가 저장/복원 후에도 줄바꿈이 유지된다.
- [ ] `data/memo.txt`가 없는 상태(첫 실행)에도 오류 없이 동작한다.

---

## 의존성

- `MainFrame.java` 수정 필요 (CalendarMemoPanel 생성 코드) → 팀장과 공유 필요.
- **SPEC-02 의존:** `MainFrame`에서 `dataManager` 필드가 있어야 합니다. SPEC-02 완료 후 진행 권장.

---

## 함정

- **줄바꿈 처리:** `JTextArea`의 `getText()`는 줄바꿈을 `\n`으로 반환합니다. 파이프 구분 파일에 `\n`을 그대로 쓰면 `readLine()`이 줄 중간에서 끊겨 데이터가 깨집니다. 반드시 `<NL>` 같은 치환 코드를 씁니다.
- **생성자 변경 파급:** `CalendarMemoPanel()`의 생성자를 `CalendarMemoPanel(DataManager dataManager)`로 바꾸면 `MainFrame`에서 호출 코드도 반드시 함께 수정해야 합니다. 하나만 바꾸면 컴파일 오류.
