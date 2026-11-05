# Day 39 — JPA 영속성 컨텍스트 심화 실습

## 프로젝트 구성 확인

- Spring Boot: `4.1.1`
- Hibernate ORM: `7.4.5.Final`
- 기존 `Member`는 `members` 테이블 Entity이며 `GenerationType.IDENTITY`를 사용한다.
- `MemberService`는 `EntityManager`와 `@Transactional` 기반이다. 새 Entity나 Spring Data Repository는 추가하지 않았다.
- 테스트 DB는 H2 메모리 DB이며 DDL은 `create-drop`이다.
- SQL과 바인딩 값은 `org.hibernate.SQL=DEBUG`, `org.hibernate.orm.jdbc.bind=TRACE`로 기록한다.

## 1차 캐시

영속성 컨텍스트는 Entity의 식별자와 객체 인스턴스를 관리한다. 같은 트랜잭션에서 같은 ID를 `find()`하면 첫 조회는 SELECT하고, 다음 조회는 관리 중인 동일 인스턴스를 돌려준다. `testFirstLevelCache`에서 참조 동일성을 검증했다.

`testClearCache`는 첫 조회 후 `clear()`를 실행한다. 기존 인스턴스는 모두 준영속 상태가 되고, 다음 `find()`는 SELECT를 다시 수행해 별도 인스턴스를 반환한다. 테스트 리포트 SQL에는 ID 2에 대한 두 SELECT가 기록됐다.

## Dirty Checking

조회한 영속 Entity의 최초 상태가 스냅샷으로 보관된다. 트랜잭션의 Flush 시점에 현재 상태와 스냅샷을 비교하여 변경된 필드를 UPDATE한다. `testDirtyChecking`은 `MemberService.updateEmail()`의 트랜잭션 커밋 후 JDBC로 변경 값을 확인했다. 실제 로그에는 `update members set email=? where id=?`와 새 이메일 바인딩 값이 있다.

`testNoChange`는 기존 이메일을 다시 설정한다. Hibernate는 스냅샷과 현재 값이 같아 UPDATE를 만들지 않는다. 로그에서도 해당 실험의 UPDATE가 없고 DB 값은 그대로다.

## Flush와 Commit

Flush는 영속성 컨텍스트 변경을 SQL로 동기화하는 과정이고, Commit은 DB 트랜잭션을 확정한다. Flush 이후에도 트랜잭션은 활성 상태이며 아직 Commit 전이면 Rollback할 수 있다.

`testFlush`에서 IDENTITY 키 생성을 위해 `persist()` 시 INSERT가 실행됐다. 이어서 명시적 `flush()`를 호출하고 같은 트랜잭션의 JDBC 조회에서 행을 확인했다. 트랜잭션 종료 후에도 행이 남아 Commit이 완료됐다. IDENTITY는 키를 DB에서 받아야 하므로 INSERT가 Flush보다 먼저 나올 수 있다.

## Flush 이후 Rollback

`testFlushAndRollback`은 테스트 메서드 자체에는 `@Transactional`을 두지 않고 `TransactionTemplate`으로 실제 Spring 트랜잭션을 열었다. `persist()`와 `flush()` 후 의도적으로 `IllegalStateException`을 던졌다. 예외는 테스트에서 정상적으로 포착했고, 트랜잭션 외부에서 JDBC로 조회한 행 수가 0임을 확인했다. SQL INSERT가 Flush되어 실행돼도 Commit 전 DB 트랜잭션을 Rollback하면 반영은 취소된다.

## JPQL 자동 Flush

`testAutoFlush`는 Entity의 이메일을 바꾼 뒤 변경된 이메일을 조건으로 JPQL 조회한다. AUTO 모드에서 Hibernate는 쿼리가 영향을 받을 수 있는 `members` 쿼리 공간과 대기 중 변경을 확인하고, 조회 결과가 최신이어야 하므로 JPQL 실행 전에 UPDATE를 Flush한다. 실제 로그의 UPDATE가 SELECT보다 먼저 나타나며 조회 결과와 최종 DB 값 모두 변경 이메일이다.

## 준영속 상태

`testDetachedEntity`에서 조회 직후 `contains()`는 `true`, `detach()` 후에는 `false`다. 준영속 상태에서 값을 바꿔도 스냅샷 비교 대상이 아니므로 UPDATE가 발생하지 않으며 DB의 기존 이메일이 유지된다.

- `detach(entity)`: 지정한 Entity 하나를 분리한다.
- `clear()`: 해당 영속성 컨텍스트의 모든 Entity를 분리한다.
- `close()`: EntityManager와 영속성 컨텍스트를 닫는다.
- `merge(detached)`: 준영속 객체의 상태를 관리 중인 인스턴스에 복사하고 그 관리 인스턴스를 반환한다. 전달한 원래 객체가 다시 영속 상태가 되는 것은 아니므로 반환값을 사용해야 한다.

## SQL 로그 관찰 결과

Hibernate 7.4.5.Final의 테스트 로그 및 `build/test-results/test/TEST-com.example.springstudy.service.MemberPersistenceContextTest.xml`을 확인했다.

| SQL | 관찰한 발생 시점 |
|---|---|
| SELECT | 최초 `find()` 및 `clear()` 뒤 재조회 |
| SELECT 생략 | 동일 컨텍스트에서 반복 `find()`; 동일 참조 반환 |
| INSERT | IDENTITY 사용 저장 시 키를 얻기 위해 `persist()`에서 실행됨 |
| UPDATE | Dirty Checking Flush; JPQL 자동 Flush 전에 실행 |
| UPDATE 생략 | 변경 값이 기존 값과 동일하거나 Entity가 detach된 경우 |
| DELETE | 기존 `MemberJpaIntegrationTest.deletesMember`와 Flush 테스트에서 확인 |

바인드 로그는 Hibernate의 `binding parameter` 행에서 확인할 수 있다. UUID를 사용해 테스트 이메일을 고유하게 만들었다. SQL 출력은 테스트 리포트에 캡처되므로 콘솔의 Gradle 요약만으로 SQL 로그를 판정하지 않았다.

## 테스트 결과

| 테스트 | 결과 |
|---|---|
| `testFirstLevelCache` | 통과 |
| `testClearCache` | 통과 |
| `testDirtyChecking` | 통과 |
| `testNoChange` | 통과 |
| `testDetachedEntity` | 통과 |
| `testFlush` | 통과 |
| `testFlushAndRollback` | 통과 (의도한 예외를 검증) |
| `testAutoFlush` | 통과 |

`./gradlew test --tests com.example.springstudy.service.MemberPersistenceContextTest`와 전체 `./gradlew test` 모두 `BUILD SUCCESSFUL`이었다. 전체 테스트 리포트에서 실패와 오류는 0건이다.

추천 커밋 메시지: `feat: Practice JPA persistence context and dirty checking`
