# API 명세서

> Swagger UI: http://localhost:8080/swagger-ui/index.html

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
| 400 | 잘못된 요청 (필수 파라미터 누락, 타입 불일치, 잘못된 JSON) |
| 404 | 리소스 없음 |
| 409 | 충돌 (정원 초과, 시간 충돌 등) |
| 500 | 서버 내부 오류 |

## 공통 에러 코드

| 에러 코드 | 상태 | 설명 |
|-----------|------|------|
| INVALID_REQUEST | 400 | 요청 데이터 유효성 검증 실패 (필수 필드 누락 등) |
| INVALID_PARAMETER | 400 | PathVariable/QueryParam 타입 불일치 (e.g., 숫자 자리에 문자열) |
| INVALID_REQUEST_BODY | 400 | 요청 본문 파싱 실패 (잘못된 JSON 등) |
| RESOURCE_NOT_FOUND | 404 | 존재하지 않는 경로 요청 |

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
    "studentNumber": "202300001",
    "grade": 3,
    "departmentName": "컴퓨터공학과"
  }
]
```

---

### 3. 강좌 목록 조회

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

**에러 케이스**:

| 상황 | 코드 | 에러 코드 | 메시지 |
|------|------|-----------|--------|
| 존재하지 않는 학과 ID | 404 | DEPARTMENT_NOT_FOUND | 해당 학과를 찾을 수 없습니다 |

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

> `studentId`와 `courseId`는 필수입니다. 누락 시 400 에러가 반환됩니다.

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
| 필수 필드 누락 | 400 | INVALID_REQUEST | studentId: 학생 ID는 필수입니다 |
| 잘못된 요청 본문 | 400 | INVALID_REQUEST_BODY | 요청 본문을 읽을 수 없습니다 |
| 학생 없음 | 404 | STUDENT_NOT_FOUND | 해당 학생을 찾을 수 없습니다 |
| 강좌 없음 | 404 | COURSE_NOT_FOUND | 해당 강좌를 찾을 수 없습니다 |
| 정원 초과 | 409 | CAPACITY_EXCEEDED | 해당 강좌의 정원이 초과되었습니다 |
| 학점 초과 | 409 | CREDIT_LIMIT_EXCEEDED | 최대 수강 학점(18학점)을 초과합니다 |
| 시간 충돌 | 409 | SCHEDULE_CONFLICT | 기존 수강 강좌와 시간이 충돌합니다 |
| 중복 신청 | 409 | ALREADY_ENROLLED | 이미 수강신청한 강좌입니다 |
| 같은 과목 타 분반 중복 | 409 | SAME_COURSE_ENROLLED | 같은 과목의 다른 분반을 이미 수강신청하였습니다 |

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
| 파라미터 타입 불일치 | 400 | INVALID_PARAMETER | 'enrollmentId' 파라미터의 값이 올바르지 않습니다 |
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
| 파라미터 타입 불일치 | 400 | INVALID_PARAMETER | 'studentId' 파라미터의 값이 올바르지 않습니다 |
| 학생 없음 | 404 | STUDENT_NOT_FOUND | 해당 학생을 찾을 수 없습니다 |

---

### 8. 내 시간표 조회 (학번)

```
GET /timetable?studentNumber={studentNumber}
```

**쿼리 파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| studentNumber | String | Y | 학번 (예: 202300001) |

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
| 학번 누락 | 400 | INVALID_PARAMETER | 'studentNumber' 파라미터가 필요합니다 |
| 학생 없음 | 404 | STUDENT_NOT_FOUND | 해당 학번의 학생을 찾을 수 없습니다 |
