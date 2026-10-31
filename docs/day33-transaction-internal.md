# Day 33 - Spring Transaction 내부 동작

이 실습은 테스트 전용 H2 데이터베이스와 Spring JDBC를 사용해 Spring AOP Proxy, Self Invocation, 그리고 Service 계층의 Transaction Boundary를 확인합니다. 실습 코드는 `src/test/java/com/example/springstudy/transaction`에 있으며, 운영 Controller·Service·Repository와 `application.yml`은 변경하지 않습니다.

## 1. Spring Proxy 구조

Spring은 컨테이너가 관리하는 Bean에 트랜잭션 Advice가 필요하다고 판단하면 대상 Bean 주변에 Proxy를 둡니다. 호출자는 Proxy를 통해 메서드를 호출하고, Proxy가 부가 동작을 적용한 뒤 실제 대상 객체의 메서드를 실행합니다. `AopUtils.isAopProxy(bean)`은 주입된 객체가 AOP Proxy인지 확인하는 데 사용합니다.

트랜잭션 설정은 `TransactionLabTestConfig`의 `@EnableTransactionManagement`가 활성화합니다. 해당 설정은 테스트용 `DriverManagerDataSource`, `JdbcTemplate`, `DataSourceTransactionManager`를 함께 등록합니다. 인터페이스가 없는 실습 Service는 클래스 기반 Proxy 대상으로 동작합니다.

## 2. TransactionInterceptor의 역할

트랜잭션 Proxy의 `TransactionInterceptor`는 호출된 메서드의 `@Transactional` 속성을 읽어 TransactionManager에 트랜잭션 시작 또는 기존 트랜잭션 참여를 요청합니다. 실제 메서드가 정상 반환하면 커밋하고, 기본 규칙에서 `RuntimeException` 또는 `Error`가 밖으로 전파되면 롤백합니다. 기본 설정은 Checked Exception만으로는 롤백하지 않으며, 필요하면 `rollbackFor`로 지정할 수 있습니다.

메서드 내부의 `TransactionSynchronizationManager.isActualTransactionActive()`는 해당 코드가 실제 트랜잭션 안에서 실행 중인지 검사합니다. DB에 실제 반영된 결과는 테스트에서 별도로 조회해 확인합니다.

## 3. `@Transactional`이 동작하지 않는 경우

`@Transactional`은 어노테이션만 붙이면 모든 호출에서 자동 적용되는 기능이 아닙니다. 대표적으로 다음 상황에서는 Proxy Advice를 통과하지 않습니다.

- Spring 컨테이너가 관리하지 않는 객체를 `new`로 직접 생성한 경우
- 같은 객체의 메서드가 `this.method()` 또는 직접 내부 호출로 실행되어 Proxy를 우회하는 경우
- Proxy 기반 트랜잭션이 가로챌 수 없는 접근 방식으로 메서드를 호출한 경우

Spring의 기본 Proxy 방식에서는 외부에서 Proxy를 통과하는 호출이 경계가 됩니다. 따라서 실습 메서드는 public으로 두고 Spring Bean 참조를 통해 호출합니다.

## 4. Self Invocation 문제

`SelfInvocationBadService`는 `execute(testId)`에서 같은 객체의 `saveAndFail(testId)`를 직접 호출합니다. Bean 자체는 Spring Proxy일 수 있지만, Proxy가 `execute` 호출을 실제 객체에 전달한 다음 target 내부에서 일어나는 `saveAndFail` 호출은 Proxy를 다시 통과하지 않습니다. 따라서 `saveAndFail`에 선언된 `@Transactional`이 적용되지 않습니다.

테스트는 메서드 진입 시 트랜잭션 활성 상태를 기록하고, INSERT 뒤 RuntimeException을 발생시킵니다. 트랜잭션이 시작되지 않았으므로 H2/JDBC 연결의 기본 자동 커밋으로 INSERT가 반영되고, 예외가 발생해도 행이 남는 것을 전용 테이블에서 확인합니다.

## 5. 직접 객체 생성 문제

`SelfInvocationBadTest`는 다음처럼 `new SelfInvocationBadService(jdbcTemplate)`로 만든 객체도 검사합니다. 이 객체는 Spring 컨테이너 Bean이 아니므로 Proxy가 없고, `AopUtils.isAopProxy()` 결과도 false입니다. 내부 메서드 호출 역시 Proxy를 통과하지 않으므로 트랜잭션 상태는 false이며 예외 뒤 INSERT 행이 남습니다.

직접 생성 테스트는 Proxy 적용 여부를 보여주는 학습용 사례입니다. 애플리케이션 Service는 직접 생성하지 않고 컨테이너가 주입한 Bean으로 사용해야 합니다.

## 6. Service 분리를 통한 해결

`SelfInvocationFacadeService`가 주입받은 `SelfInvocationTransactionalService`를 호출하도록 역할을 나눴습니다. Facade에서 다른 Spring Bean을 호출하므로 호출은 트랜잭션 Service의 Proxy를 통과하고, `saveAndFail` 실행 전에 트랜잭션이 시작됩니다.

INSERT 이후 RuntimeException을 던지면 Proxy가 예외를 감지해 롤백합니다. 테스트는 트랜잭션 상태가 true였고, 전용 테이블에서 해당 식별자의 행이 0건임을 확인합니다.

## 7. Transaction 적용 위치

트랜잭션은 여러 DB 작업을 하나의 업무 단위로 원자적으로 처리해야 하는 경계에 둡니다. 이 프로젝트의 주문 예제에서는 주문 저장, 주문 상품 저장, 처리 이력 저장을 하나의 주문 생성 작업으로 묶으므로 Service 메서드에 `@Transactional`을 적용합니다.

Repository는 각각 SQL 실행과 테이블 접근을 담당합니다. Repository 메서드마다 독립적인 업무 트랜잭션을 시작하면 앞 Repository의 변경만 커밋되고 뒤 작업은 실패하는 식의 부분 반영이 생길 수 있습니다. Repository가 기존 Service 트랜잭션에 참여하는 구성은 가능하지만, 업무 경계는 Service에서 명시하는 편이 명확합니다.

## 8. Service Layer와 Transaction Boundary

`TransactionBoundaryService.createOrderSuccessfully(orderId)`는 세 Repository를 호출하고 정상 종료합니다. 세 행은 모두 커밋됩니다. `createOrderWithFailure(orderId)`도 세 INSERT를 수행한 뒤 RuntimeException을 던지며, 세 행 모두 롤백됩니다.

`TransactionBoundaryTest`는 다음 사항을 함께 확인합니다.

- Service가 AOP Proxy인지 확인
- Service 메서드 실행 중 실제 트랜잭션이 활성 상태인지 확인
- `JdbcTemplate`과 `DataSourceTransactionManager`가 같은 `DataSource` 인스턴스를 사용하는지 확인
- 각 테이블을 실제 조회해 커밋 결과와 롤백 결과 확인

주문 예제의 세 전용 테이블은 `transaction-boundary-schema.sql`에서 초기화합니다. 기존 Day 31·32 테이블과 이름이 겹치지 않습니다.

## 9. 잘못된 코드와 수정 코드 비교

| 구현 | Proxy 통과 | 메서드 안 트랜잭션 | 예외 후 행 |
|---|---:|---:|---:|
| Spring Bean의 Self Invocation | `saveAndFail` 호출은 통과하지 않음 | 비활성 | INSERT 1건 잔존 |
| `new`로 만든 Service | Proxy 없음 | 비활성 | INSERT 1건 잔존 |
| Facade → 별도 트랜잭션 Service | 트랜잭션 Service Proxy 통과 | 활성 | INSERT 롤백, 0건 |
| 주문 Service 성공 경로 | Service Proxy 통과 | 활성 | 주문·상품·이력 각각 1건 커밋 |
| 주문 Service 실패 경로 | Service Proxy 통과 | 활성 | 세 테이블 모두 0건 롤백 |

## 10. 테스트 결과

2026-10-31 실행 결과:

| 범위 | 결과 |
|---|---|
| Day 31: `TransactionPracticeTest`, `TransactionProxyTest`, `ReadOnlyTransactionTest` | BUILD SUCCESSFUL |
| Day 32: `TransactionPropagationTest`, `TransactionIsolationTest` | BUILD SUCCESSFUL |
| Day 33: `SelfInvocationBadTest`, `SelfInvocationFixedTest` | BUILD SUCCESSFUL |
| Day 33: `TransactionBoundaryTest` | BUILD SUCCESSFUL |
| 전체 `./gradlew test` | BUILD SUCCESSFUL |

각 Day 33 테스트는 UUID 기반 키를 사용하며 `@AfterEach`에서 테스트 데이터를 정리합니다. 테스트 메서드 자체에는 `@Transactional`을 적용하지 않아 Service 트랜잭션의 실제 커밋 및 롤백을 조회할 수 있습니다.

> `SelfInvocationBadService`, `SelfInvocationTransactionalService`, `TransactionBoundaryService`의 `volatile` 상태 값은 학습 테스트가 메서드 내부의 활성 상태를 관찰하기 위한 장치입니다. 운영 Service의 요청별 상태 저장 방식으로 사용하지 마세요. 동시 호출까지 고려하는 운영 설계에서는 테스트 전용 관측 수단이나 별도 실행 결과 전달을 사용해야 합니다.
