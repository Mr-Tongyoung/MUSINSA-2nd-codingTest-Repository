# 동시성 제어 전략 5가지 동작 원리

> 수강신청 시나리오: **정원 1명 남은 강좌에 학생 A, B가 동시에 신청**하는 상황을 기준으로 각 전략의 동작 원리를 설명한다.

---

## 1. 비관적 락 (Pessimistic Lock)

### 핵심 개념

"충돌이 발생할 것이라고 **비관적으로 가정**하고, 데이터를 읽는 시점에 미리 잠근다."

### 동작 원리

DB의 `SELECT ... FOR UPDATE` 구문으로 행(row)에 배타적 잠금을 건다. 잠금을 획득한 트랜잭션이 커밋 또는 롤백할 때까지 다른 트랜잭션은 해당 행을 수정할 수 없고 **대기**한다.

### 동작 흐름

```
학생 A                              학생 B
   │                                   │
   ├─ BEGIN TRANSACTION                ├─ BEGIN TRANSACTION
   │                                   │
   ├─ SELECT * FROM course             ├─ SELECT * FROM course
   │  WHERE id=1 FOR UPDATE            │  WHERE id=1 FOR UPDATE
   │                                   │
   ├─ 🔒 락 획득 성공                    ├─ ⏳ 락 대기 (블로킹)
   │  enrolled=29, capacity=30         │     ...
   │                                   │     ...
   ├─ enrolled < capacity → 통과       │     ...
   ├─ INSERT enrollment                │     ...
   ├─ UPDATE enrolled = 30             │     ...
   │                                   │     ...
   ├─ COMMIT                           │
   │                                   │
   ├─ 🔓 락 해제                        ├─ 🔒 락 획득 성공
                                       │  enrolled=30, capacity=30
                                       │
                                       ├─ enrolled >= capacity → 거절
                                       ├─ ROLLBACK
                                       │
                                       ├─ 🔓 락 해제
```

### JPA 구현

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Course c WHERE c.id = :id")
Optional<Course> findByIdWithLock(@Param("id") Long id);
```

`PESSIMISTIC_WRITE`는 내부적으로 `SELECT ... FOR UPDATE`를 생성한다.

### 특징

| 항목 | 설명 |
|------|------|
| 잠금 시점 | 데이터를 **읽는 시점** |
| 잠금 범위 | 행(row) 단위 |
| 충돌 처리 | 대기 (블로킹) |
| 재시도 | 불필요 (대기 후 순차 처리) |
| 데드락 | 여러 행을 서로 다른 순서로 잠그면 발생 가능 |

---

## 2. 낙관적 락 (Optimistic Lock)

### 핵심 개념

"충돌이 거의 발생하지 않을 것이라고 **낙관적으로 가정**하고, 잠금 없이 진행한 뒤 커밋 시점에 충돌을 감지한다."

### 동작 원리

엔티티에 `version` 필드를 두고, UPDATE 시 `WHERE version = ?` 조건을 포함한다. 읽은 시점의 version과 커밋 시점의 version이 다르면 다른 트랜잭션이 먼저 수정한 것이므로 `OptimisticLockException`이 발생한다.

### 동작 흐름

```
학생 A                                학생 B
   │                                     │
   ├─ BEGIN                              ├─ BEGIN
   │                                     │
   ├─ SELECT * FROM course WHERE id=1    ├─ SELECT * FROM course WHERE id=1
   │  → enrolled=29, version=5           │  → enrolled=29, version=5
   │                                     │
   ├─ (잠금 없음, 양쪽 모두 읽기 성공)       │
   │                                     │
   ├─ UPDATE course                      ├─ UPDATE course
   │  SET enrolled=30, version=6         │  SET enrolled=30, version=6
   │  WHERE id=1 AND version=5           │  WHERE id=1 AND version=5
   │                                     │
   ├─ ✅ 1행 변경 → 성공                   ├─ ❌ 0행 변경 → version 불일치!
   ├─ COMMIT                             ├─ OptimisticLockException 발생
   │                                     │
   │                                     ├─ ROLLBACK
   │                                     ├─ 🔄 재시도 (처음부터 다시)
   │                                     │
   │                                     ├─ SELECT → enrolled=30, version=6
   │                                     ├─ enrolled >= capacity → 거절
```

### JPA 구현

```java
@Entity
public class Course {
    @Version
    private Long version;
    // ...
}
```

`@Version`을 붙이면 JPA가 UPDATE 쿼리에 자동으로 `AND version = ?` 조건을 추가한다.

### 재시도 로직 (애플리케이션에서 직접 구현 필요)

```java
int maxRetry = 3;
for (int i = 0; i < maxRetry; i++) {
    try {
        enrollmentService.enroll(studentId, courseId);
        break;
    } catch (OptimisticLockException e) {
        if (i == maxRetry - 1) throw e;
        // 잠시 대기 후 재시도
    }
}
```

### 특징

| 항목 | 설명 |
|------|------|
| 잠금 시점 | 잠금 없음 (**커밋 시점에 충돌 감지**) |
| 잠금 범위 | 없음 (version 비교로 대체) |
| 충돌 처리 | 예외 발생 → 롤백 → 재시도 |
| 재시도 | **필수** (애플리케이션에서 직접 구현) |
| Retry Storm | 정원 1명 × 100명 동시 → 99명이 반복 롤백+재시도 |

### 비관적 락 vs 낙관적 락 비교

```
비관적 락:  A 잠금 → A 처리 → A 해제 → B 잠금 → B 처리 → B 해제
           (순차 대기, 불필요한 작업 없음)

낙관적 락:  A 읽기, B 읽기 → A 커밋 성공 → B 커밋 실패 → B 재시도 → B 읽기 → B 커밋
           (실패한 작업을 처음부터 다시 수행)
```

경합이 높을수록(수강신청처럼) 낙관적 락의 재시도 비용이 급격히 증가한다.

---

## 3. 분산 락 (Redis)

### 핵심 개념

"DB가 아닌 **외부 시스템(Redis)** 에서 잠금을 관리하여 DB 부하를 줄이고, 다중 서버 환경에서도 동작하게 한다."

### 동작 원리

Redis의 `SET key value NX PX timeout` 명령으로 락을 획득한다. `NX`(Not eXists)는 키가 없을 때만 설정하므로, 먼저 요청한 쪽만 락을 획득한다. 다른 요청은 키가 이미 존재하므로 획득 실패한다.

### 동작 흐름

```
학생 A                                  학생 B
   │                                       │
   ├─ SET course:1:lock A_ID NX PX 3000   ├─ SET course:1:lock B_ID NX PX 3000
   │  → OK (키 없었음, 락 획득)              │  → nil (키 이미 존재, 락 실패)
   │                                       │
   │                                       ├─ ⏳ 재시도 대기 or 즉시 실패
   │                                       │
   ├─ BEGIN TRANSACTION                    │
   ├─ SELECT enrolled FROM course          │
   │  → enrolled=29                        │
   ├─ INSERT enrollment                    │
   ├─ UPDATE enrolled = 30                 │
   ├─ COMMIT                              │
   │                                       │
   ├─ DEL course:1:lock (락 해제)           │
   │                                       │
   │                                       ├─ SET course:1:lock B_ID NX PX 3000
   │                                       │  → OK (락 획득)
   │                                       │
   │                                       ├─ SELECT enrolled → 30
   │                                       ├─ 정원 초과 → 거절
   │                                       ├─ DEL course:1:lock (락 해제)
```

### Redis 명령어 해석

```
SET course:1:lock "requestId_A" NX PX 3000
│   │              │             │  │  │
│   │              │             │  │  └─ 3000ms 후 자동 만료 (안전장치)
│   │              │             │  └──── PX: 만료 시간을 밀리초로 지정
│   │              │             └─────── NX: 키가 없을 때만 설정
│   │              └───────────────────── 값: 요청 식별자 (본인만 해제 가능하게)
│   └──────────────────────────────────── 키: 강좌별 고유 락 이름
└──────────────────────────────────────── SET 명령
```

### Redisson (Java 라이브러리) 구현

```java
RLock lock = redissonClient.getLock("course:" + courseId);
try {
    // 최대 3초 대기, 획득 시 5초 후 자동 해제
    if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
        enrollmentService.enroll(studentId, courseId);
    } else {
        throw new BusinessException("락 획득 실패");
    }
} finally {
    lock.unlock();
}
```

Redisson의 Watchdog은 비즈니스 로직이 예상보다 오래 걸릴 경우 락의 TTL을 자동으로 연장해준다.

### 특징

| 항목 | 설명 |
|------|------|
| 잠금 시점 | 비즈니스 로직 **진입 전** (Redis에서) |
| 잠금 범위 | 키(key) 단위 (자유로운 범위 설정) |
| 충돌 처리 | 락 획득 실패 → 대기 or 즉시 실패 |
| 재시도 | 선택적 (tryLock 대기 시간 설정) |
| 추가 인프라 | **Redis 서버 필요** |
| 다중 서버 | 지원 (핵심 장점) |

### DB 락과의 차이

```
DB 비관적 락:   DB 커넥션 획득 → DB 락 획득 → 처리 → 커밋 → DB 락 해제
               (락 대기 중에도 DB 커넥션을 점유)

Redis 분산 락:  Redis 락 획득 → DB 커넥션 획득 → 처리 → 커밋 → Redis 락 해제
               (락 대기 중에는 DB 커넥션을 점유하지 않음)
```

---

## 4. 네임드 락 (Named Lock / Advisory Lock)

### 핵심 개념

"DB가 제공하는 **사용자 정의 이름 기반 잠금**으로, 테이블 행이 아닌 임의의 문자열을 기준으로 잠근다."

### 동작 원리

MySQL의 `GET_LOCK(name, timeout)` 함수로 이름 기반 잠금을 획득한다. 이 잠금은 테이블이나 행과 무관한 **메타데이터 수준의 잠금**이므로, 다른 트랜잭션의 SELECT/UPDATE에 영향을 주지 않는다.

### 동작 흐름

```
학생 A                                     학생 B
   │                                          │
   ├─ [커넥션 1] GET_LOCK('course_1', 3)      ├─ [커넥션 1] GET_LOCK('course_1', 3)
   │  → 1 (획득 성공)                           │  → ⏳ 3초 대기
   │                                          │
   ├─ [커넥션 2] BEGIN TRANSACTION             │
   ├─ [커넥션 2] SELECT enrolled → 29          │
   ├─ [커넥션 2] INSERT enrollment             │
   ├─ [커넥션 2] UPDATE enrolled = 30          │
   ├─ [커넥션 2] COMMIT                        │
   │                                          │
   ├─ [커넥션 1] RELEASE_LOCK('course_1')      │
   │                                          │
   │                                          ├─ 1 (획득 성공)
   │                                          ├─ [커넥션 2] BEGIN
   │                                          ├─ [커넥션 2] SELECT enrolled → 30
   │                                          ├─ 정원 초과 → 거절
   │                                          ├─ [커넥션 2] ROLLBACK
   │                                          ├─ [커넥션 1] RELEASE_LOCK('course_1')
```

### MySQL 함수

```sql
-- 락 획득 (최대 3초 대기, 성공 시 1, 타임아웃 시 0, 에러 시 NULL)
SELECT GET_LOCK('course_enroll_42', 3);

-- 비즈니스 로직 수행...

-- 락 해제 (성공 시 1)
SELECT RELEASE_LOCK('course_enroll_42');

-- 락 보유 여부 확인
SELECT IS_FREE_LOCK('course_enroll_42');
```

### 커넥션 분리가 필요한 이유

Named Lock은 **커넥션(세션)에 바인딩**된다. 같은 커넥션에서 비즈니스 트랜잭션을 수행하면:

```
[같은 커넥션 - 잘못된 방식]
GET_LOCK → BEGIN → INSERT → COMMIT → RELEASE_LOCK
                                      │
                                      └─ 트랜잭션이 커밋되어도 RELEASE_LOCK 전까지
                                         다른 요청이 대기해야 함

[커넥션 분리 - 올바른 방식]
커넥션 1: GET_LOCK ──────────────────── RELEASE_LOCK
커넥션 2:           BEGIN → 처리 → COMMIT
```

커넥션을 분리하면 Named Lock의 획득/해제와 비즈니스 트랜잭션이 독립적으로 관리된다.

### 특징

| 항목 | 설명 |
|------|------|
| 잠금 시점 | 비즈니스 로직 **진입 전** (DB 메타데이터 레벨) |
| 잠금 범위 | **문자열 이름** 단위 (테이블/행과 무관) |
| 충돌 처리 | 타임아웃까지 대기 |
| 재시도 | 불필요 (대기 후 순차 처리) |
| 조회 영향 | **없음** (행 잠금이 아니므로 SELECT에 영향 없음) |
| DB 지원 | MySQL (`GET_LOCK`), PostgreSQL (`pg_advisory_lock`), **H2 미지원** |

### 비관적 락 vs 네임드 락 비교

```
비관적 락:  SELECT * FROM course WHERE id=1 FOR UPDATE
           → course 테이블의 id=1 행이 잠김
           → 다른 트랜잭션이 같은 행을 FOR UPDATE로 읽으면 대기
           → 일반 SELECT도 격리 수준에 따라 영향받을 수 있음

네임드 락:  GET_LOCK('course_1', 3)
           → 'course_1'이라는 이름만 잠김
           → course 테이블 자체는 자유롭게 조회 가능
           → 같은 이름으로 GET_LOCK 호출한 요청만 대기
```

---

## 5. 큐 기반 순차 처리 (Message Queue)

### 핵심 개념

"동시 요청을 **큐(Queue)에 순서대로 적재**하고, 컨슈머가 한 건씩 순차 처리하여 동시성 문제를 **구조적으로 제거**한다."

### 동작 원리

수강신청 요청을 Kafka/RabbitMQ 같은 메시지 큐에 발행(produce)하고, 컨슈머가 하나씩 꺼내어 처리한다. `course_id`를 파티션 키로 사용하면 같은 강좌에 대한 요청이 같은 파티션에 순서대로 적재되어 **단일 컨슈머가 순차 처리**한다.

### 동작 흐름

```
학생 A, B, C가 동시에 course_id=1에 수강신청

[API 서버 - 즉시 응답]

학생 A ─── POST /enrollments ──→ API 서버 ──→ "접수 완료" (즉시 응답)
학생 B ─── POST /enrollments ──→ API 서버 ──→ "접수 완료" (즉시 응답)
학생 C ─── POST /enrollments ──→ API 서버 ──→ "접수 완료" (즉시 응답)

[Kafka Topic - course_id로 파티셔닝]

Partition 0 (course_id=1): ┌─────┬─────┬─────┐
                           │  A  │  B  │  C  │  ← 도착 순서대로 적재
                           └─────┴─────┴─────┘

Partition 1 (course_id=2): (다른 강좌 요청은 다른 파티션)

[Consumer - 순차 처리]

Consumer가 Partition 0에서 하나씩 꺼내어 처리:

A 처리: enrolled=29 → 30 (성공) → 결과 알림: "수강신청 성공"
B 처리: enrolled=30 → 정원 초과 (실패) → 결과 알림: "정원 초과"
C 처리: enrolled=30 → 정원 초과 (실패) → 결과 알림: "정원 초과"
```

### 결과 알림 방식

큐 기반 처리는 **비동기**이므로, 처리 결과를 클라이언트에 전달하는 별도 메커니즘이 필요하다:

```
방식 1: 폴링 (Polling)
  클라이언트가 주기적으로 결과를 조회
  GET /enrollments/status?requestId=abc123
  → { "status": "PROCESSING" }  (아직 처리 중)
  → { "status": "SUCCESS" }     (처리 완료)

방식 2: WebSocket / SSE
  서버가 처리 완료 시 클라이언트에 즉시 푸시
  → 실시간 알림 가능하지만 구현 복잡도 증가

방식 3: 콜백 (Callback)
  처리 완료 시 지정된 URL로 결과를 POST
  → 서버 간 통신에 적합, 클라이언트 직접 알림에는 부적합
```

### Kafka 파티셔닝의 핵심

```
같은 파티션 키(course_id) → 같은 파티션 → 같은 컨슈머 → 순차 처리
   ↓
동시성 문제가 구조적으로 제거됨 (잠금 자체가 불필요)
```

```
course_id=1 → hash(1) % 3 = 1 → Partition 1 → Consumer A (순차 처리)
course_id=2 → hash(2) % 3 = 2 → Partition 2 → Consumer B (순차 처리)
course_id=3 → hash(3) % 3 = 0 → Partition 0 → Consumer C (순차 처리)

→ 서로 다른 강좌는 병렬 처리, 같은 강좌는 순차 처리
```

### 특징

| 항목 | 설명 |
|------|------|
| 잠금 | **없음** (순차 처리로 동시성 구조적 제거) |
| 충돌 처리 | 충돌 자체가 발생하지 않음 |
| 사용자 응답 | **비동기** (즉시 "접수 완료" → 결과는 나중에 알림) |
| 처리 순서 | 보장 (파티션 내 FIFO) |
| 추가 인프라 | **Kafka/RabbitMQ 필요** |
| 구현 복잡도 | 높음 (Producer, Consumer, 결과 알림 모두 구현) |
| 트래픽 흡수 | 스파이크 트래픽을 큐가 버퍼링하여 서버를 보호 |

---

## 전략 종합 비교

### 동작 방식 비교

| 전략 | 잠금 위치 | 충돌 시 행동 | 응답 방식 |
|------|:--------:|:----------:|:--------:|
| **비관적 락** | DB 행(row) | 대기 (블로킹) | 동기 |
| **낙관적 락** | 없음 (version 비교) | 롤백 + 재시도 | 동기 |
| **분산 락** | Redis (외부) | 대기 or 즉시 실패 | 동기 |
| **네임드 락** | DB 메타데이터 | 대기 (블로킹) | 동기 |
| **큐 기반** | 없음 (순차 처리) | 충돌 없음 | 비동기 |

### 적합한 상황

| 전략 | 최적 상황 | 부적합 상황 |
|------|----------|-----------|
| **비관적 락** | 단일 서버 + 높은 경합 | 조회 성능이 중요한 경우 |
| **낙관적 락** | 낮은 경합 (충돌 드묾) | 높은 경합 (retry storm 발생) |
| **분산 락** | 다중 서버 환경 | 단일 서버 (오버스펙) |
| **네임드 락** | 단일 서버 + 조회 성능 중요 | H2 등 미지원 DB |
| **큐 기반** | 극한 트래픽 + 비동기 허용 | 즉시 응답이 필수인 경우 |

### 수강신청 시나리오에서의 선택

```
단일 서버 + H2        → 비관적 락 ✅ (현재 프로젝트)
단일 서버 + MySQL     → 비관적 락 또는 네임드 락
다중 서버 + MySQL     → Redis 분산 락
극한 트래픽 + 비동기   → 큐 기반 순차 처리
낮은 경합 (선착순 아님) → 낙관적 락
```
