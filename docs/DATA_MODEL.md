# 데이터 모델 (Data Model)

앱은 DB 없이 `data/` 폴더 안의 텍스트 파일에 데이터를 저장합니다.

---

## 파일 목록

| 파일 | 담당 클래스 | 저장 내용 |
|------|------------|----------|
| `data/todo_list.txt` | `DataManager` | 할 일 목록 전체 |
| `data/pomodoro_stats.txt` | `DataManager` | 오늘의 집중 통계 |

> `data/` 폴더는 앱 실행 시 자동 생성됩니다. `.gitignore`에 포함되어 있으므로 Git에 올라가지 않아요.

---

## todo_list.txt

### 형식
```
title|completed|targetDate
```

### 필드 설명
| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| `title` | String | 할 일 내용 | `자바 Chapter 5 읽기` |
| `completed` | boolean | 완료 여부 (`true`/`false`) | `false` |
| `targetDate` | String (ISO 8601) | 목표 날짜 | `2026-05-24` |

### 예시
```
자바 Chapter 5 읽기|false|2026-05-24
알고리즘 문제 3개 풀기|true|2026-05-24
영어 단어 50개 암기|false|2026-05-24
```

### 주의사항
- 구분자는 `|` (파이프) 한 글자입니다.
- `title` 안에 `|` 문자가 들어가면 파싱이 깨집니다. 입력 시 `|` 금지.
- 한 줄 = 할 일 1개. 빈 줄 없이 작성.
- 파일이 없으면 `loadTodos()`는 빈 리스트를 반환합니다 (정상 동작).

---

## pomodoro_stats.txt

### 형식
```
date|completedPomodoros|totalFocusMinutes
```

### 필드 설명
| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| `date` | String (ISO 8601) | 해당 날짜 | `2026-05-24` |
| `completedPomodoros` | int | 완료한 뽀모도로 횟수 | `4` |
| `totalFocusMinutes` | int | 누적 집중 시간 (분 단위) | `100` |

### 예시
```
2026-05-24|4|100
```

### 주의사항
- 항상 **1줄**만 저장합니다 (오늘 통계만 유지).
- 날짜가 오늘과 다르면 `loadTodayStats()`는 새 빈 통계를 반환합니다 (날짜 자동 리셋).
- 과거 날짜 히스토리는 현재 저장하지 않습니다 (Phase 2 확장 영역).

---

## 확장 시 필드 추가 방법

필드를 추가하려면 반드시 `DataManager`의 파싱 코드와 동시에 수정해야 합니다.

예: `targetDate` 뒤에 `priority` 필드 추가 시
```java
// DataManager.loadTodos() 에서 parts[3] 추가
todos.add(new Todo(parts[0], Boolean.parseBoolean(parts[1]), parts[2], parts[3]));

// DataManager.saveTodos() 에서 직렬화 추가
writer.write(todo.getTitle() + "|" + todo.isCompleted() + "|" + todo.getTargetDate() + "|" + todo.getPriority());
```

> 기존 파일이 있는 상태에서 필드를 추가하면 파싱 오류가 납니다. `data/todo_list.txt`를 삭제하거나 필드를 수동으로 추가해야 합니다.
