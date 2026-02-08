# 대학교 수강신청 시스템

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 4.0.2 |
| 빌드 도구 | Gradle |
| 데이터베이스 | H2 (인메모리) |
| API 형식 | REST API |

## 빌드 방법

```bash
./gradlew build
```

## 실행 방법

```bash
./gradlew bootRun
```

## API 서버 접속 정보

| 항목 | 값 |
|------|------|
| 호스트 | localhost |
| 포트 | 8080 |
| 헬스체크 | GET http://localhost:8080/health |

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
│   └── *.md
└── src/                       소스 코드
```