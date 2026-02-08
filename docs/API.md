# API 명세서

> 이 문서는 구현 진행에 따라 업데이트됩니다.

## Base URL

```
http://localhost:8080
```

## 공통 에러 응답 형식

```json
{
  "error": "에러 메시지",
  "code": "ERROR_CODE"
}
```

## 공통 HTTP 상태 코드

| 코드 | 의미 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 400 | 잘못된 요청 |
| 404 | 리소스 없음 |
| 409 | 충돌 (정원 초과, 시간 충돌 등) |
| 500 | 서버 내부 오류 |

---

## 엔드포인트 목록

### 1. 헬스체크

```
GET /health
```

**응답**: `200 OK`

---

### 2. 학생 목록 조회

```
GET /students
```

**응답 예시**:
```json
[
  {
    "id": 1,
    "name": "김민준",
    "departmentName": "컴퓨터공학과",
    "grade": 3
  }
]
```

---

### 3. 강좌 목록 조회 (전체)

```
GET /courses
```

**쿼리 파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| departmentId | Long | N | 학과 ID로 필터링 |

**응답 예시**:
```json
[
  {
    "id": 1,
    "name": "자료구조",
    "credits": 3,
    "capacity": 30,
    "enrolled": 25,
    "schedule": "월 09:00-10:30",
    "professorName": "이영희",
    "departmentName": "컴퓨터공학과"
  }
]
```

---

### 4. 교수 목록 조회

```
GET /professors
```

**응답 예시**:
```json
[
  {
    "id": 1,
    "name": "이영희",
    "departmentName": "컴퓨터공학과"
  }
]
```

---

### 5. 수강신청

```
POST /enrollments
```

**요청**:
```json
{
  "studentId": 1,
  "courseId": 1
}
```

**성공 응답** `201 Created`:
```json
{
  "id": 1,
  "studentId": 1,
  "courseId": 1,
  "courseName": "자료구조",
  "credits": 3,
  "schedule": "월 09:00-10:30"
}
```

**에러 케이스**:

| 상황 | 코드 | 에러 코드 | 메시지 |
|------|------|-----------|--------|
| 정원 초과 | 409 | CAPACITY_EXCEEDED | 해당 강좌의 정원이 초과되었습니다 |
| 학점 초과 | 409 | CREDIT_LIMIT_EXCEEDED | 최대 수강 학점(18학점)을 초과합니다 |
| 시간 충돌 | 409 | SCHEDULE_CONFLICT | 기존 수강 강좌와 시간이 충돌합니다 |
| 중복 신청 | 409 | ALREADY_ENROLLED | 이미 수강신청한 강좌입니다 |
| 학생 없음 | 404 | STUDENT_NOT_FOUND | 해당 학생을 찾을 수 없습니다 |
| 강좌 없음 | 404 | COURSE_NOT_FOUND | 해당 강좌를 찾을 수 없습니다 |

---

### 6. 수강취소

```
DELETE /enrollments/{enrollmentId}
```

**성공 응답** `200 OK`:
```json
{
  "message": "수강취소가 완료되었습니다"
}
```

**에러 케이스**:

| 상황 | 코드 | 에러 코드 | 메시지 |
|------|------|-----------|--------|
| 수강 기록 없음 | 404 | ENROLLMENT_NOT_FOUND | 해당 수강 기록을 찾을 수 없습니다 |

---

### 7. 내 시간표 조회

```
GET /students/{studentId}/timetable
```

**응답 예시**:
```json
{
  "studentId": 1,
  "studentName": "김민준",
  "semester": "2026-1",
  "totalCredits": 15,
  "courses": [
    {
      "enrollmentId": 1,
      "courseId": 1,
      "courseName": "자료구조",
      "professorName": "이영희",
      "credits": 3,
      "schedule": "월 09:00-10:30"
    }
  ]
}
```

**에러 케이스**:

| 상황 | 코드 | 에러 코드 | 메시지 |
|------|------|-----------|--------|
| 학생 없음 | 404 | STUDENT_NOT_FOUND | 해당 학생을 찾을 수 없습니다 |
