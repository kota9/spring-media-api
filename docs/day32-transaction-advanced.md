# Day 32 - Transaction 심화 실습

이 문서는 Day 31 테스트 전용 H2 환경 위에 추가한 Transaction Propagation 및 Isolation 실습을 설명합니다. 실습은 운영 DataSource나 운영 Service를 변경하지 않고 `src/test`의 `TransactionLabTestConfig`가 제공하는 H2 `DataSource`, `JdbcTemplate`, `DataSourceTransactionManager`를 재사용합니다.

## 1. Transaction Propagation

Propagation은 트랜잭션 메서드가 호출될 때 현재 트랜잭션과 어떻게 관계를 맺을지 정합니다. 실습 코드는 별도의 Spring Bean을 호출해 프록시가 실제로 전파 규칙을 적용하게 합니다.

- [PropagationOuterService](../src/test/java/com/example/springstudy/transaction/PropagationOuterService.java)는 외부 트랜잭션을 시작하고 내부 서비스를 호출합니다.
- [PropagationInnerService](../src/test/java/com/example/springstudy/transaction/PropagationInnerService.java)는 REQUIRED와 REQUIRES_NEW 메서드를 제공합니다.
- [TransactionPropagationTest](../src/test/java/com/example/springstudy/transaction/TransactionPropagationTest.java)는 `AopUtils.isAopProxy`와 JdbcTemplate 조회로 프록시 및 DB 결과를 검증합니다.
- [transaction-propagation-schema.sql](../src/test/resources/transaction-propagation-schema.sql)은 Propagation 실습 전용 테이블을 만듭니다.

### REQUIRED

`Propagation.REQUIRED`는 기존 트랜잭션이 있으면 그 트랜잭션에 참여하고, 없으면 새 트랜잭션을 시작합니다. `saveRequired`는 외부 서비스가 시작한 트랜잭션에 참여합니다.

```java
@Transactional(propagation = Propagation.REQUIRED)
public void saveRequired(String operationId) {
    save(operationId, "INNER_REQUIRED", "required inner record");
}
```

`executeRequiredThenFail`가 외부 행과 내부 행을 기록한 뒤 런타임 예외를 던지면 두 행 모두 롤백됩니다. 테스트는 해당 `operation_id` 조회 결과가 비어 있는지 확인합니다.

### REQUIRES_NEW

`Propagation.REQUIRES_NEW`는 기존 트랜잭션을 중단하고 독립 트랜잭션을 시작합니다. 내부 메서드가 반환되기 전에 내부 트랜잭션이 커밋되며, 바깥 트랜잭션은 다시 이어집니다.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveRequiresNew(String operationId) {
    save(operationId, "INNER_REQUIRES_NEW", "requires-new inner record");
}
```

`executeRequiresNewThenFail`에서는 외부 TX1이 롤백되어 외부 행은 사라지지만, 이미 커밋한 내부 TX2 행은 남습니다.

### REQUIRED와 REQUIRES_NEW 비교

| 호출 상황 | 내부 트랜잭션 | 외부가 롤백될 때 |
|---|---|---|
| REQUIRED | 기존 외부 트랜잭션에 참여 | 내부 변경도 롤백 |
| REQUIRES_NEW | 외부를 중단하고 독립 트랜잭션 시작 | 내부 커밋은 유지 |

내부 REQUIRED 메서드가 런타임 예외를 던지면 참여한 트랜잭션이 rollback-only로 표시됩니다. 외부가 예외를 잡고 정상 반환하더라도 커밋할 수 없으므로 트랜잭션 완료 시 `UnexpectedRollbackException`이 발생합니다. 테스트는 최종 예외와 두 행의 롤백을 함께 확인합니다.

REQUIRES_NEW는 별도 Connection과 추가 DB 자원을 필요로 합니다. 외부 트랜잭션이 Connection을 점유한 상태에서 내부 트랜잭션이 새 Connection을 요청하므로, 실제 Connection Pool이 작은 환경에서는 풀 고갈이나 대기가 발생할 수 있습니다.

## 2. Transaction Isolation

Isolation은 동시에 실행되는 트랜잭션이 서로의 변경을 어떤 시점과 범위로 볼지 정합니다. [IsolationReaderService](../src/test/java/com/example/springstudy/transaction/IsolationReaderService.java)는 격리 수준을 `@Transactional(isolation = …)`으로 지정하고, [IsolationWriterService](../src/test/java/com/example/springstudy/transaction/IsolationWriterService.java)는 별도 트랜잭션에서 행을 수정하거나 추가합니다.

[TransactionIsolationTest](../src/test/java/com/example/springstudy/transaction/TransactionIsolationTest.java)는 고정 스레드 2개를 가진 `ExecutorService`, 타임아웃이 있는 `CountDownLatch`, 제한 시간이 있는 `Future.get`으로 작업 순서를 맞춥니다. Reader와 Writer는 서로 다른 스레드에서 실행되고, 실제 바인딩된 JDBC Connection 객체가 다름을 검사합니다. 테스트 종료 시 `finally`에서 ExecutorService를 종료합니다. 각 테스트는 전용 테이블을 비우고 UUID 기반 키를 써서 실행 순서에 의존하지 않습니다.

[transaction-isolation-schema.sql](../src/test/resources/transaction-isolation-schema.sql)은 `record_key`, `name`, `amount`, `category`를 가진 격리 전용 테이블을 생성합니다.

### Dirty Read

Dirty Read는 다른 트랜잭션이 아직 커밋하지 않은 변경을 읽는 현상입니다. 테스트에서는 Writer가 `amount`를 100에서 200으로 바꾸고 커밋을 보류한 동안 READ_UNCOMMITTED Reader가 값을 조회합니다. Reader가 조회를 마칠 때까지 Writer가 대기하므로, 읽힌 200은 커밋 전 값임이 보장됩니다.

H2 2.4.240에서 실제로 200을 읽어 Dirty Read가 관찰됐습니다. 단, DB 엔진과 버전에 따라 READ_UNCOMMITTED가 지원되지 않거나 더 강한 격리로 동작할 수 있으므로 해당 결과를 모든 DB에 일반화하면 안 됩니다.

### Non-Repeatable Read

Non-Repeatable Read는 한 트랜잭션이 같은 행을 다시 읽었을 때, 다른 트랜잭션의 커밋으로 값이 바뀌어 보이는 현상입니다. Reader가 첫 값을 읽고 대기하는 동안 Writer가 별도 트랜잭션으로 값을 변경하고 커밋합니다.

실습 결과 READ_COMMITTED에서는 `100 → 200`, REPEATABLE_READ에서는 `100 → 100`이었습니다.

### Phantom Read

Phantom Read는 동일한 조건으로 행 집합을 다시 조회할 때, 다른 트랜잭션이 조건에 맞는 행을 추가·커밋해 결과 행 개수가 달라지는 현상입니다. 실습에서는 `category`별 개수를 두 번 조회하고, 조회 사이에 Writer가 같은 범주에 행을 삽입합니다.

실습 결과 READ_COMMITTED에서는 `1 → 2`였고, REPEATABLE_READ에서는 이번 H2 실행에서 `1 → 1`로 관찰됐습니다. 테스트는 REPEATABLE_READ의 실제 결과를 기록하고 1 또는 2를 허용하여 특정 DB 동작을 강제로 맞추지 않습니다.

## 3. Isolation Level 비교

SQL 표준의 일반적인 현상 기준과 H2에서 이 프로젝트가 관찰한 결과를 구분합니다.

| Isolation Level | SQL 표준의 일반적인 허용 현상 | H2 2.4.240 실습 관찰 |
|---|---|---|
| READ_UNCOMMITTED | Dirty, Non-Repeatable, Phantom Read 가능 | Dirty Read 발생 (100 대신 미커밋 200 관찰) |
| READ_COMMITTED | Dirty Read 방지, Non-Repeatable·Phantom Read 가능 | Non-Repeatable Read 100→200, Phantom Read 1→2 |
| REPEATABLE_READ | Dirty·Non-Repeatable Read 방지, Phantom Read는 허용될 수 있음 | Non-Repeatable Read 100→100, Phantom Read 1→1 |
| SERIALIZABLE | 세 현상 방지 목표의 가장 강한 표준 격리 수준 | Spring 트랜잭션에 SERIALIZABLE 설정이 적용됨을 확인. 세 현상 동시성 실험은 이 수준에 대해 수행하지 않음 |

DB마다 MVCC, 잠금, 스냅샷 구현이 다르므로 표준의 허용 현상은 특정 구현의 실제 결과와 같다고 단정할 수 없습니다. 특히 REPEATABLE_READ의 Phantom Read 실습 결과는 이 H2 버전과 이 조회에서 얻은 관찰값입니다. SERIALIZABLE 테스트도 설정 적용을 확인했지만, 동시 쓰기 상황의 완전한 직렬 실행 동등성을 시험한 것은 아닙니다.

## 4. Transaction Boundary

실제 애플리케이션에서 트랜잭션 경계는 보통 하나의 비즈니스 작업을 완결하는 Service 메서드에 둡니다. 그 작업에 필요한 DB 변경을 묶고 메서드가 끝나면 커밋 또는 롤백합니다. 다른 Service를 호출해도 프록시를 통하면 같은 트랜잭션에 참여할 수 있습니다. 같은 객체 내부의 self-invocation은 프록시를 우회할 수 있습니다. Day 31의 [TransactionProxyTest](../src/test/java/com/example/springstudy/transaction/TransactionProxyTest.java)는 외부 프록시 호출과 self-invocation 차이를 확인합니다.

현재 운영 Service와 Repository는 메모리 기반이며 프로덕션 코드에서 `@Transactional`이나 외부 API 클라이언트를 찾지 못했습니다. 그러므로 운영용 트랜잭션 경계의 적절성이나 장시간 외부 호출 포함 여부를 이 프로젝트의 실제 DB 트랜잭션으로 평가할 수는 없습니다. 위 Service 트랜잭션 코드는 테스트 전용 학습 코드입니다.

DB 연동 시에는 DB 작업의 일관성이 필요한 최소 비즈니스 구간을 트랜잭션으로 묶고, HTTP/API 호출이나 긴 파일 처리 같은 지연 작업을 그 안에서 기다리지 않는 설계를 권합니다. DB 커밋과 외부 시스템 변경을 원자적으로 묶어야 한다면 Outbox 등 별도 패턴을 고려합니다. 서비스 간 호출은 공개 프록시 메서드를 통과하게 하고, 무거운 독립 처리가 꼭 필요할 때만 REQUIRES_NEW를 사용합니다.

## 5. 테스트 실행 및 결과

Day 31 Transaction 테스트:

```powershell
.\gradlew.bat test --tests "com.example.springstudy.transaction.TransactionPracticeTest" --tests "com.example.springstudy.transaction.TransactionProxyTest" --tests "com.example.springstudy.transaction.ReadOnlyTransactionTest"
```

Day 32 Propagation 및 Isolation 테스트:

```powershell
.\gradlew.bat test --tests "com.example.springstudy.transaction.TransactionPropagationTest" --tests "com.example.springstudy.transaction.TransactionIsolationTest"
```

프로젝트 전체 테스트:

```powershell
.\gradlew.bat test
```

리뷰 시 Isolation 테스트 클래스를 `--rerun-tasks`로 5회 실제 재실행했고 5회 모두 통과했습니다. 전체 Gradle 테스트도 통과했습니다. 최종 전체 실행 결과는 **42개 테스트, 실패 0, 오류 0, 건너뜀 0**입니다.
