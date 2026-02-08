# AI 에이전트 지침 (CLAUDE.md)

## 프로젝트 개요

대학교 수강신청 시스템 백엔드 API 서버를 구현하는 프로젝트이다.
Spring Boot 4.0.2 + Java 17 + Gradle 기반이며, H2 인메모리 DB를 사용한다.

## 핵심 원칙

1. **동작 우선**: 빌드되고 실행 가능한 상태를 항상 유지할 것
2. **핵심 기능 우선**: 수강신청/취소, 동시성 제어를 가장 먼저 구현할 것
3. **동시성 안전**: 정원 초과가 절대 발생하지 않도록 할 것

## 기술 규칙

- REST API 원칙을 준수할 것
- 모든 API는 JSON 형식으로 요청/응답할 것
- 에러 응답은 일관된 형식을 유지할 것
- Lombok을 활용하여 보일러플레이트 코드를 줄일 것

## 비즈니스 규칙

- 학생당 최대 18학점까지 수강신청 가능
- 동일 시간대에 두 개 이상의 강좌 수강 불가
- 강좌 정원 초과 수강신청 불가 (동시성 제어 필수)
- 동일 강좌 중복 수강신청 불가
- 같은 과목의 다른 분반 중복 수강신청 불가

## 데이터 규모

- 학과: 10개 이상
- 강좌: 500개 이상
- 학생: 10,000명 이상
- 교수: 100명 이상
- 서버 시작 후 1분 이내 데이터 생성 완료

## 코드 스타일

- 패키지 구조: controller / service / repository / entity / dto / config
- 클래스명: PascalCase
- 메서드/변수명: camelCase
- API 응답 필드명: camelCase

## 커밋 컨벤션

- `Init:` 초기 설정
- `Feat:` 새로운 기능
- `Fix:` 버그 수정
- `Refactor:` 리팩토링
- `Docs:` 문서 작업
- `Test:` 테스트

## 필수 엔드포인트

- `GET /health` - 헬스체크
- `GET /students` - 학생 목록 조회
- `GET /courses` - 강좌 목록 조회
- `GET /professors` - 교수 목록 조회
- `POST /enrollments` - 수강신청
- `DELETE /enrollments/{id}` - 수강취소
- `GET /students/{id}/timetable` - 내 시간표 조회
