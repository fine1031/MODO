# SPEC-01: TODO 수정 기능

**왜:** 할 일 제목을 잘못 입력했을 때 삭제 후 재입력 없이 바로 고칠 수 있어야 한다.

---

## 건드릴 파일

- `src/model/Todo.java` — `setTitle()` 이미 있음, 추가 없음
- `src/service/TodoService.java` — `updateTodo(int index, String newTitle)` 메서드 추가
- `src/ui/TodoPanel.java` — 각 행에 "수정" 버튼 추가 + 다이얼로그 호출

> **이 파일 외에는 건드리지 마세요.**

---

## 구현 가이드

### 1. TodoService에 updateTodo() 추가

```java
public void updateTodo(int index, String newTitle) {
    String trimmed = newTitle.trim();
    if (index < 0 || index >= todos.size() || trimmed.isEmpty()) {
        return;
    }
    todos.get(index).setTitle(trimmed);
    saveTodos();
}
```

### 2. TodoPanel의 createTodoRow()에 "수정" 버튼 추가

```java
JButton editButton = new JButton("수정");
editButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
editButton.addActionListener(e -> {
    String input = JOptionPane.showInputDialog(this, "수정할 내용을 입력하세요:", todo.getTitle());
    if (input != null && !input.trim().isEmpty()) {
        todoService.updateTodo(index, input);
        refreshTodos();
    }
});

// 기존 rightPanel에 editButton 추가
rightPanel.add(editButton);
rightPanel.add(deleteButton);
```

---

## 성공 기준

- [ ] TodoPanel의 각 할 일 행에 "수정" 버튼이 보인다.
- [ ] "수정" 클릭 시 기존 내용이 채워진 입력 다이얼로그가 뜬다.
- [ ] 새 제목 입력 후 확인 시 목록에 즉시 반영된다.
- [ ] 앱을 재실행해도 수정된 제목이 유지된다 (파일에 저장됨).
- [ ] 빈 문자열 입력 또는 취소 시 아무것도 변경되지 않는다.

---

## 의존성

- 다른 팀원 작업과 충돌 없음. `TodoPanel`과 `TodoService`만 수정.
- `DataManager`는 수정 없음. 기존 `saveTodos()`가 그대로 동작.

---

## 함정

- **인덱스 불일치:** `createTodoRow()`에서 `index`를 람다에 캡처할 때 `final int i = index`로 선언해야 합니다. 람다 안에서 변수가 변경되면 컴파일 오류가 납니다.
- **빈 문자열 처리:** `JOptionPane.showInputDialog()`는 취소 시 `null`, 빈 확인 시 `""`를 반환합니다. 두 경우 모두 처리해야 합니다.
- **UI 즉시 반영:** `todoService.updateTodo()` 호출 후 반드시 `refreshTodos()`를 호출해야 화면이 갱신됩니다.
