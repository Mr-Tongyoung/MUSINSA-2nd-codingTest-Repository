# 대학교 수강신청 시스템

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 4.0.2 |
| 빌드 도구 | Gradle 9.3 |
| 데이터베이스 | H2 (인메모리) |
| ORM | Spring Data JPA (Hibernate 7.2) |
| API 형식 | REST API |

## 빌드 방법

```bash
./gradlew build
```

## 실행 방법

```bash
./gradlew bootRun
```

## 테스트 실행

```bash
./gradlew test
```

- 동시성 테스트 4개 + 비즈니스 규칙 테스트 10개 = 총 14개

## API 서버 접속 정보

| 항목 | 값 |
|------|------|
| 호스트 | localhost |
| 포트 | 8080 |
| 헬스체크 | `GET http://localhost:8080/health` |
| H2 콘솔 | `http://localhost:8080/h2-console/` |
| H2 JDBC URL | `jdbc:h2:mem:coursedb` |
| H2 사용자명 | sa |
| H2 비밀번호 | (없음) |

## 초기 데이터

서버 시작 시 자동 생성 (약 5초 소요):

| 항목 | 수량 |
|------|------|
| 학과 | 15개 |
| 교수 | 137명 |
| 강좌 | 547개 |
| 학생 | 10,000명 |

## Swagger UI (API 문서)

서버 실행 후 브라우저에서 접속:

```
http://localhost:8080/swagger-ui/index.html
```

- 전체 API 목록 조회, 요청 파라미터 확인, 직접 API 호출 테스트 가능
- springdoc-openapi 기반으로 컨트롤러에서 자동 생성

## API 엔드포인트 요약

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/health` | 헬스체크 |
| GET | `/students` | 학생 목록 조회 |
| GET | `/courses` | 강좌 목록 조회 (전체) |
| GET | `/courses?departmentId={id}` | 강좌 목록 조회 (학과별) |
| GET | `/professors` | 교수 목록 조회 |
| POST | `/enrollments` | 수강신청 |
| DELETE | `/enrollments/{id}` | 수강취소 |
| GET | `/students/{id}/timetable` | 내 시간표 조회 |

> 상세 API 명세는 [docs/API.md](docs/API.md) 참고

## 프로젝트 구조

```
├── README.md                  빌드 및 실행 방법
├── CLAUDE.md                  AI 에이전트 지침
├── PROBLEM.md                 과제 요구사항
├── build.gradle               빌드 설정
├── docs/
│   ├── REQUIREMENTS.md        요구사항 분석 및 설계 결정
│   └── API.md                 API 명세서
├── prompts/                   AI 프롬프트 이력
│   └── PROMPT-HISTORY.md
└── src/
    ├── main/java/.../
    │   ├── config/            DataInitializer, H2ConsoleConfig
    │   ├── controller/        REST 컨트롤러 (5개)
    │   ├── dto/               요청/응답 DTO (7개)
    │   ├── entity/            JPA 엔티티 (5개)
    │   ├── exception/         예외 처리
    │   ├── repository/        JPA 리포지토리 (5개)
    │   └── service/           비즈니스 로직 (4개)
    └── test/java/.../
        └── service/           동시성 + 비즈니스 규칙 테스트
```
