# 프롬프트 #001 - 프로젝트 초기 설정

- **입력 시간**: 2026-02-08
- **도구**: Claude Code (Claude Opus 4.6)

---

## 프롬프트 1

> 대학교 수강신청 프로그램을 만들기 위한 프로젝트를 진행하자. 첫번째 커밋 메세지 작성해줘.

### 수행 결과
- Spring Boot 프로젝트 초기 커밋 생성
- `.DS_Store`를 스테이징에서 제거
- 커밋 메시지: `Init: Spring Boot 프로젝트 초기 설정`

---

## 프롬프트 2

> https://github.com/Mr-Tongyoung/MUSINSA-2nd-codingTest-Repository.git 원격 깃 저장소의 링크이다. 메인 브랜치에 푸쉬 해줘

### 수행 결과
- origin 원격 저장소에 main 브랜치 푸시 완료

---

## 프롬프트 3

> PROBLEM.md를 분석해서 프로젝트를 시작하자.
> 1. 제출물 구조를 확인한뒤에 필수적인 제출 파일들을 생성
> 2. 지금부터 입력하는 모든 프롬프트는 prompts/*.md 파일에 입력시간과 함께 저장

### 수행 결과
- 필수 제출 파일 생성: README.md, CLAUDE.md, docs/REQUIREMENTS.md, docs/API.md
- 프롬프트 이력 기록 시작

---

## 프롬프트 4

> 커밋하고 푸시해줘

### 수행 결과
- 커밋: `Docs: 필수 제출 문서 및 프로젝트 구조 설정`
- origin/main 푸시 완료

---

## 프롬프트 5

> 이제 본격적으로 핵심 기능 구현을 시작하자. 요구사항을 분석하여 앞으로의 작업단위를 만들어줘

### 수행 결과
- 7개 작업 단위 생성 (의존관계 포함)

---

## 프롬프트 6

> 응 시작하자

### 수행 결과 (2026-02-08 15:38)
- Task #1~#5 전체 구현 완료
- H2 DB + JPA 엔티티 5개 (Department, Professor, Course, Student, Enrollment)
- Repository 5개 (비관적 락 쿼리 포함)
- 초기 데이터 생성기: 학과 15, 교수 137, 강좌 547, 학생 10,000
- 조회 API: GET /health, /students, /courses, /professors
- 수강신청/취소 API: POST /enrollments, DELETE /enrollments/{id}, GET /students/{id}/timetable
- 동시성 제어: 비관적 락 (PESSIMISTIC_WRITE)
- 서버 기동 5초, 전체 API 정상 동작 확인

---

## 프롬프트 7

> 인메모리 디비가 정상 작동하는지 웹브라우저에서 콘솔로 확인했는데 Whitelabel Error Page 가 나오는 중임. 문제 원인 분석

### 수행 결과 (2026-02-08 15:45)
- 원인: Spring Boot 4.x에서 H2ConsoleAutoConfiguration 미작동
- 해결: JakartaWebServlet 수동 서블릿 등록 (H2ConsoleConfig.java)
- H2 의존성 runtimeOnly → implementation 변경

---

## 프롬프트 8

> 작업내용 성격에 따라 분류하여 커밋하고 푸시해줘

### 수행 결과 (2026-02-08 15:50)
- 3개 커밋으로 분류: Feat(엔티티), Feat(API+동시성), Fix(H2콘솔)
- origin/main 푸시 완료

---

## 프롬프트 9

> 다음 작업 진행하자. 동시성 통합 테스트 작성해줘

### 수행 결과 (2026-02-08 16:04)
- EnrollmentConcurrencyTest: 동시성 테스트 4개
  - 정원 1명 강좌 100명 동시 신청 → 1명만 성공
  - 정원 30명 강좌 100명 동시 신청 → 30명만 성공
  - 동일 학생 동시 중복 신청 → 1번만 성공
  - 수강신청/취소 동시 발생 → enrolled 카운트 정확
- EnrollmentServiceTest: 비즈니스 규칙 테스트 9개
  - 수강신청 성공, 정원 초과, 중복 신청, 학점 초과, 시간 충돌 등
- 전체 13개 테스트 PASSED

---

## 프롬프트 10

> 커밋하고 푸시해줘

### 수행 결과 (2026-02-08 16:10)
- 커밋: `Test: 동시성 통합 테스트 및 비즈니스 규칙 테스트 작성`
- origin/main 푸시 완료

---

## 프롬프트 11

> 문서 최종 업데이트 및 마무리 진행

### 수행 결과 (2026-02-08 16:15)
- README.md: 포트 8081 반영, 테스트 실행 방법, H2 콘솔 접속 정보, 초기 데이터 규모, 프로젝트 구조 상세화
- docs/API.md: Base URL 8081 반영, 학생 응답에 studentNumber 필드 추가
- docs/REQUIREMENTS.md: 동시성 검증 결과, 시간 충돌 로직 상세화, 데이터 정합성 보장, 테스트 전략 추가
- prompts/ 프롬프트 이력 최종 업데이트
