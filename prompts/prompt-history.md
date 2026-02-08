# AI 프롬프트 이력

- **프로젝트**: 대학교 수강신청 시스템
- **도구**: Claude Code (Claude Opus 4.6)
- **작업일**: 2026-02-08

---

## 프롬프트 1

> 대학교 수강신청 프로그램을 만들기 위한 프로젝트를 진행하자. 첫번째 커밋 메세지 작성해줘.

### 답변
Spring Boot 4.0.2 + Java 17 + Gradle 기반으로 프로젝트를 초기 세팅하고, `.DS_Store`를 `.gitignore`에 추가하여 첫 커밋을 생성.

### 수행 결과
- 커밋: `Init: Spring Boot 프로젝트 초기 설정`

---

## 프롬프트 2

> https://github.com/Mr-Tongyoung/MUSINSA-2nd-codingTest-Repository.git 원격 깃 저장소의 링크이다. 메인 브랜치에 푸쉬 해줘

### 답변
origin 원격 저장소를 등록하고 main 브랜치에 푸시.

### 수행 결과
- origin/main 푸시 완료

---

## 프롬프트 3

> PROBLEM.md를 분석해서 프로젝트를 시작하자.
> 1. 제출물 구조를 확인한뒤에 필수적인 제출 파일들을 생성
> 2. 지금부터 입력하는 모든 프롬프트는 prompts/*.md 파일에 입력시간과 함께 저장

### 답변
PROBLEM.md의 제출물 구조를 분석하여 필수 파일 4개(README.md, CLAUDE.md, docs/REQUIREMENTS.md, docs/API.md)를 식별하고 초기 내용으로 생성. 프롬프트 이력 기록 체계를 수립.

### 수행 결과
- 필수 제출 파일 4개 생성
- prompts/ 디렉토리에 프롬프트 이력 기록 시작

---

## 프롬프트 4

> 커밋하고 푸시해줘

### 수행 결과
- 커밋: `Docs: 필수 제출 문서 및 프로젝트 구조 설정`
- origin/main 푸시 완료

---

## 프롬프트 5

> 이제 본격적으로 핵심 기능 구현을 시작하자. 요구사항을 분석하여 앞으로의 작업단위를 만들어줘

### 답변
PROBLEM.md의 요구사항을 분석하여 의존관계를 고려한 7개 작업 단위로 분해:
1. H2 DB 설정 + JPA 엔티티 (Department, Professor, Course, Student, Enrollment)
2. Repository 계층 (JpaRepository + 비관적 락 쿼리)
3. 초기 데이터 생성기 (학과 10+, 교수 100+, 강좌 500+, 학생 10,000+)
4. 헬스체크 및 조회 API (/health, /students, /courses, /professors)
5. 수강신청/취소 API + 동시성 제어 (핵심)
6. 동시성 통합 테스트 (100명 동시 신청 → 1명만 성공 검증)
7. 문서 최종 업데이트

핵심 판단: 동시성 제어에 비관적 락(PESSIMISTIC_WRITE)을 채택. 수강신청은 경합이 높은 시나리오이므로 낙관적 락의 재시도 오버헤드보다 행 수준 락의 확실성이 적합.

### 수행 결과
- 7개 작업 단위 생성 (의존관계 DAG 포함)

---

## 프롬프트 6

> 응 시작하자

### 답변
작업 #1~#5를 순차 구현. 엔티티 5개, 리포지토리 5개, DataInitializer(ApplicationRunner), 컨트롤러 5개, 서비스 4개를 생성. 수강신청 시 `CourseRepository.findByIdWithLock()`으로 Course 행을 비관적 락으로 잠근 뒤 정원/학점/시간 충돌/중복 검증을 수행하는 구조로 구현.

### 수행 결과
- JPA 엔티티 5개, Repository 5개 (비관적 락 포함)
- 초기 데이터: 학과 15, 교수 137, 강좌 547, 학생 10,000 (서버 기동 ~5초)
- 조회 API 4개 + 수강신청/취소/시간표 API 3개 = 총 7개 엔드포인트
- 비즈니스 규칙: 정원 초과, 18학점 제한, 시간 충돌, 중복 신청 방지

---

## 프롬프트 7

> 인메모리 디비가 정상 작동하는지 웹브라우저에서 콘솔로 확인했는데 Whitelabel Error Page 가 나오는 중임. 문제 원인 분석

### 답변
Spring Boot 4.x에서 H2ConsoleAutoConfiguration이 정상 작동하지 않는 것이 원인. H2의 `JakartaWebServlet`을 `ServletRegistrationBean`으로 수동 등록하여 해결. H2 의존성도 `runtimeOnly` → `implementation`으로 변경 필요 (수동 서블릿 등록 시 컴파일 타임 접근 필요).

### 수행 결과
- H2ConsoleConfig.java 생성 (JakartaWebServlet 수동 서블릿 등록)
- H2 의존성 runtimeOnly → implementation 변경
- `/h2-console/` 정상 접근 확인

---

## 프롬프트 8

> 작업내용 성격에 따라 분류하여 커밋하고 푸시해줘

### 수행 결과
- 3개 커밋으로 분류: `Feat: 엔티티/리포지토리`, `Feat: API+동시성 제어`, `Fix: H2 콘솔`
- origin/main 푸시 완료

---

## 프롬프트 9

> 다음 작업 진행하자. 동시성 통합 테스트 작성해줘

### 답변
`ExecutorService` + `CountDownLatch`를 활용한 동시성 테스트와 단위 비즈니스 규칙 테스트를 분리하여 작성. 동시성 테스트는 실제 멀티스레드 환경에서 DB 락이 정상 동작하는지 검증.

### 수행 결과
- EnrollmentConcurrencyTest (4개): 정원 1명×100명 동시, 정원 30명×100명 동시, 동일 학생 중복 동시, 신청/취소 동시
- EnrollmentServiceTest (9개): 성공, 정원 초과, 중복, 학점 초과, 시간 충돌, 경계 시간, 취소, 미존재 취소, 시간표 조회
- 전체 13개 테스트 PASSED

---

## 프롬프트 10

> 커밋하고 푸시해줘

### 수행 결과
- 커밋: `Test: 동시성 통합 테스트 및 비즈니스 규칙 테스트 작성`
- origin/main 푸시 완료

---

## 프롬프트 11

> 문서 최종 업데이트 및 마무리 진행

### 답변
README.md, API.md, REQUIREMENTS.md를 실제 구현 결과에 맞게 갱신. 포트, 초기 데이터 규모, 프로젝트 구조, 동시성 검증 결과, 시간 충돌 로직 상세, 테스트 전략 등을 반영.

### 수행 결과
- README.md: 테스트 실행 방법, H2 콘솔 접속 정보, 초기 데이터 규모, 프로젝트 구조 상세화
- docs/API.md: 학생 응답에 studentNumber 필드 추가
- docs/REQUIREMENTS.md: 동시성 검증 결과, 시간 충돌 로직 상세화, 데이터 정합성, 테스트 전략 추가

---

## 프롬프트 12

> 포트번호는 8080으로 설정하고, 이에 맞게 포트 넘버 관련 문서 업데이트

### 답변
application.yaml의 server.port를 8080으로 설정하고, README.md와 API.md의 포트 번호를 일괄 수정.

### 수행 결과
- application.yaml: server.port → 8080
- README.md, docs/API.md: 포트 번호 8080으로 통일

---

## 프롬프트 13

> 커밋하고 푸시해줘

### 수행 결과
- 커밋: `Docs: 문서 최종 업데이트 및 포트 8080 통일`
- origin/main 푸시 완료

---

## 프롬프트 14

> SwaggerAPI를 사용하여 API 접근성을 높여라. Swagger 의존성을 주입하고 각각의 컨트롤러에 스웨거독스를 추가한다.

### 답변
springdoc-openapi-starter-webmvc-ui를 도입하고, SwaggerConfig에서 OpenAPI 메타정보를 설정. 5개 컨트롤러에 `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter` 어노테이션을 추가하여 Swagger UI에서 전체 API를 인터랙티브하게 테스트 가능하도록 구성.

### 수행 결과
- springdoc-openapi-starter-webmvc-ui 2.8.6 의존성 추가
- SwaggerConfig.java 생성
- 컨트롤러 5개에 Swagger 어노테이션 추가
- Swagger UI 정상 동작 확인: http://localhost:8080/swagger-ui/index.html

---

## 프롬프트 15

> 커밋하고 푸시해줘

### 수행 결과
- 커밋: `Feat: Swagger(OpenAPI) 문서 적용`
- origin/main 푸시 완료

---

## 프롬프트 16

> 각 API의 엣지 케이스를 찾는다.
> 예시: 현재 학생조회 API는 학생이 존재하지 않을 경우는 엣지 케이스이다. 이와같은 엣지 케이스를 찾고 예외처리 후 관련 문서 업데이트 진행.

### 답변
전체 API를 순회하며 6개 엣지 케이스를 식별:
1. `POST /enrollments` - studentId/courseId null → Bean Validation(@Valid + @NotNull) 적용
2. `POST /enrollments` - 잘못된 JSON body → HttpMessageNotReadableException 핸들러 추가
3. `DELETE /enrollments/abc` - PathVariable 타입 불일치 → MethodArgumentTypeMismatchException 핸들러 추가
4. `GET /students/abc/timetable` - 동일 타입 불일치 처리
5. `GET /courses?departmentId=99999` - 존재하지 않는 학과 → DEPARTMENT_NOT_FOUND 검증 추가
6. `GET /nonexistent` - 없는 경로 → NoResourceFoundException 핸들러 추가

spring-boot-starter-validation 의존성이 누락되어 있어 함께 추가.

### 수행 결과
- build.gradle: spring-boot-starter-validation 의존성 추가
- EnrollmentRequest: @NotNull 검증 추가
- EnrollmentController: @Valid 추가
- CourseService: 학과 존재 여부 검증 추가
- GlobalExceptionHandler: 4개 예외 핸들러 추가
- docs/API.md: 공통 에러 코드 및 각 엔드포인트 에러 케이스 업데이트

---

## 프롬프트 17

> 커밋하고 푸시해줘

### 수행 결과
- 커밋: `Fix: API 엣지 케이스 예외 처리 강화`
- origin/main 푸시 완료

---

## 프롬프트 18

> 같은 과목은 분반이 다르더라도 수강신청할 수 없게 처리

### 답변
Course 엔티티에 `baseName` 필드를 추가하여 분반과 무관한 원래 과목명을 저장 (예: "자료구조 1분반" → baseName: "자료구조"). 수강신청 시 `existsByStudentIdAndCourseBaseName` 쿼리로 같은 baseName의 과목이 이미 등록되어 있으면 `SAME_COURSE_ENROLLED`로 거절.

### 수행 결과
- Course 엔티티: baseName 필드 추가
- DataInitializer: 분반 생성 시 baseName 설정
- EnrollmentRepository: existsByStudentIdAndCourseBaseName JPQL 쿼리 추가
- EnrollmentService: 같은 과목 타 분반 중복 검증 로직 추가
- EnrollmentServiceTest: 같은 과목 다른 분반 수강신청 테스트 추가
- docs/API.md: SAME_COURSE_ENROLLED 에러 코드 추가
- 전체 테스트 통과 (14개)

---

## 프롬프트 19

> 커밋하고 푸시해줘

### 수행 결과
- 커밋: `Feat: 같은 과목 타 분반 수강신청 중복 방지`
- origin/main 푸시 완료

---

## 프롬프트 20

> PROBLEM.md를 다시 확인하고 평가 안내에 나와있는 기준을 모두 만족했는지 검사

### 답변
PROBLEM.md의 평가 기준 3단계(동작 여부, 핵심 기능, 사고의 깊이)와 제출물 구조, 데이터 요구사항을 전체 대조. 모든 항목 충족 확인. 단, 3건의 미반영 사항 발견: README 테스트 개수 오류(13→14), CLAUDE.md 분반 규칙 누락, REQUIREMENTS.md 분반 규칙 누락.

### 수행 결과
- 평가 기준 전체 대조 점검 완료
- 미반영 사항 3건 식별

---

## 프롬프트 21

> 미반영 사항에 대해 업데이트

### 수행 결과
- README.md: 테스트 개수 13개 → 14개 수정
- CLAUDE.md: 같은 과목 타 분반 중복 수강신청 불가 규칙 추가
- docs/REQUIREMENTS.md: 암묵적 요구사항 및 테스트 전략에 SAME_COURSE_ENROLLED 반영

---

## 프롬프트 22

> 암묵적 요구사항에 대해서 단순히 불필요하다. 제한이 없다. 등의 근거보다는 해당 기능을 구현하기 위해 필요한 오버헤드(DB 데이터추가 등)를 언급하고 추후 리팩토링을 하기 위한 방안에 어떤것이 있는지 정리

### 답변
미구현 4개 항목에 대해 단순 "불필요" 대신 구체적 분석으로 개선:
- **인증/인가**: User 테이블, BCrypt, JWT, SecurityFilterChain 필요 → Spring Security + JWT 도입으로 기존 서비스 로직 변경 최소화 가능
- **수강취소 기한**: enrolledAt 필드, EnrollmentPeriod 엔티티 필요 → cancelDeadline과 현재 시각 비교로 구현
- **재수강**: Semester/EnrollmentHistory/Grade 엔티티 필요 → 이전 학기 성적 조회 후 재수강 조건(F/D 이하) 검증
- **선수과목/학년 제한**: course_prerequisite 조인 테이블, minGrade 필드 필요 → 다대다 자기참조로 선수과목 매핑

### 수행 결과
- docs/REQUIREMENTS.md: 암묵적 요구사항 섹션을 구현 완료/미구현으로 분리, 미구현 4개 항목에 현재 상태·구현 오버헤드·향후 방안 기술

---

## 프롬프트 23

> 프롬프트 이력 관리 파일명을 직관적으로 변환, 프롬프트 이력 업데이트, 변경사항 커밋 후 푸시

### 답변
파일명을 `001-project-setup.md` → `prompt-history.md`로 변경하여 채점자가 프롬프트 이력임을 즉시 파악할 수 있도록 개선. 누락된 프롬프트 19~23 이력 추가. 프롬프트·답변·수행 결과 3단 템플릿으로 전면 재구성.

### 수행 결과
- prompts/001-project-setup.md → prompts/prompt-history.md 파일명 변경
- 프롬프트 19~23 이력 추가
- 전체 프롬프트에 답변(핵심 결론) 섹션 추가
- README.md 프로젝트 구조 반영
