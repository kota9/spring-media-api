# Spring Media API

Spring Boot 기반의 REST API 학습 프로젝트입니다. User, Content, Category, Comment API와 기존 Product, Media 학습 API를 포함합니다.

## 학습 목표

- IoC와 DI, Spring Bean 및 ApplicationContext 이해
- 계층형 아키텍처 설계
- REST API, DTO Validation, 전역 예외 처리 구현
- 메모리 저장소를 이용한 데이터 관계 검증

## 기술 스택

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC, Jackson, Jakarta Validation
- Gradle, JUnit 5, Spring Boot Test, MockMvc

JPA, MyBatis, 외부 데이터베이스는 사용하지 않습니다.

## 실행 방법

사전 준비: JDK 21.

Windows:

```bash
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
./gradlew build
./gradlew test
./gradlew bootRun
```

애플리케이션은 기본 포트 `8080`에서 실행됩니다. 별도 프로파일이나 비밀 설정은 필요하지 않습니다. 저장소는 메모리 기반이므로 재시작하면 데이터가 초기화됩니다.

JSON 요청 본문이 있는 POST·PUT·PATCH 요청에는 `Content-Type: application/json` 헤더를 사용합니다. GET·DELETE 요청에는 본문이 필요하지 않습니다. 각 생성 API는 `201 Created`와 생성된 리소스 응답, `Location` 헤더를 반환합니다.

## 패키지 구조

```text
src/main/java/com/example/springstudy
├── SpringStudyApplication.java
├── controller/  # HTTP 엔드포인트: User, Content, Category, Comment, Product, Media
├── service/     # CRUD, 입력 규칙 및 데이터 관계 검증
├── repository/  # 메모리 저장소
├── domain/      # User, Content, Category, Comment 등의 도메인 모델
├── dto/         # 요청·응답 DTO와 공통 오류 응답
└── exception/   # 사용자 정의 예외, 오류 코드, ApiExceptionHandler
```

## 공통 응답 및 오류 처리

요청 본문이 있는 API는 `Content-Type: application/json`을 사용합니다. 오류 응답은 다음 공통 형식입니다.

```json
{
  "timestamp": "2026-01-01T12:00:00",
  "status": 404,
  "code": "CONTENT_NOT_FOUND",
  "message": "콘텐츠를 찾을 수 없습니다.",
  "path": "/api/contents/999999999"
}
```

실제 `timestamp`와 `path`는 응답 시점과 요청 URL에 따라 달라집니다.

| 상태 | 오류 코드 | 의미 |
|---|---|---|
| 400 | `INVALID_REQUEST` | Bean Validation 또는 요청 JSON 오류 |
| 400 | `INVALID_CONTENT_PATCH` | 빈 PATCH 또는 유효하지 않은 PATCH 필드 |
| 404 | `USER_NOT_FOUND` | 회원을 찾을 수 없음 |
| 404 | `CONTENT_NOT_FOUND` | 게시글을 찾을 수 없음 |
| 404 | `CATEGORY_NOT_FOUND` | 카테고리를 찾을 수 없음 |
| 404 | `COMMENT_NOT_FOUND` | 댓글을 찾을 수 없음 |
| 409 | `DUPLICATE_USER_EMAIL` | 이미 등록된 이메일 |
| 500 | `INTERNAL_SERVER_ERROR` | 예상하지 못한 서버 오류 |

## User API

회원은 `name`, `email` 필드를 사용합니다. 비밀번호·로그인·인증 기능은 구현되어 있지 않습니다.

| 메서드 | URL | 성공 상태 |
|---|---|---|
| POST | `/api/users` | 201 Created |
| GET | `/api/users` | 200 OK |
| GET | `/api/users/{userId}` | 200 OK |
| PUT | `/api/users/{userId}` | 200 OK |
| DELETE | `/api/users/{userId}` | 204 No Content |

POST와 PUT 요청 예시:

```json
{
  "name": "홍길동",
  "email": "hong@example.com"
}
```

이름은 필수이며 2~20자, 이메일은 필수이며 이메일 형식이어야 합니다. 이메일이 이미 사용 중이면 `409 DUPLICATE_USER_EMAIL`, 없는 회원 ID는 `404 USER_NOT_FOUND`를 반환합니다. 응답 필드는 `id`, `name`, `email`입니다.

생성·조회·수정 응답 예시 (`201` 생성, `200` 조회·수정):

```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com"
}
```

## Content API

게시글은 `title`, `description` 필드를 사용합니다.

| 메서드 | URL | 성공 상태 |
|---|---|---|
| POST | `/api/contents` | 201 Created |
| GET | `/api/contents` | 200 OK |
| GET | `/api/contents/{id}` | 200 OK |
| PUT | `/api/contents/{id}` | 200 OK |
| PATCH | `/api/contents/{id}` | 200 OK |
| DELETE | `/api/contents/{id}` | 204 No Content |

POST와 PUT 요청 예시:

```json
{
  "title": "Spring 학습 기록",
  "description": "게시글 내용입니다."
}
```

제목은 필수이며 100자 이하, 설명은 필수이며 공백만 입력할 수 없습니다. PATCH에는 변경할 필드만 포함하며 빈 객체, 명시적 `null`, 공백 제목·설명, 100자를 넘는 제목은 `400 INVALID_CONTENT_PATCH`로 거부됩니다.

응답 필드는 `id`, `title`, `description`입니다. 없는 게시글 ID는 `404 CONTENT_NOT_FOUND`입니다. 게시글 삭제 시 연결된 댓글도 함께 삭제됩니다.

생성·조회·수정 응답 예시 (`201` 생성, `200` 조회·수정):

```json
{
  "id": 1,
  "title": "Spring 학습 기록",
  "description": "게시글 내용입니다."
}
```

## Category API

카테고리는 필수 `name`과 선택 `description`을 사용합니다.

| 메서드 | URL | 성공 상태 |
|---|---|---|
| POST | `/api/categories` | 201 Created |
| GET | `/api/categories` | 200 OK |
| GET | `/api/categories/{categoryId}` | 200 OK |
| PUT | `/api/categories/{categoryId}` | 200 OK |
| DELETE | `/api/categories/{categoryId}` | 204 No Content |

요청 예시:

```json
{
  "name": "학습",
  "description": "학습 관련 게시글"
}
```

이름은 공백일 수 없고 100자 이하입니다. 설명은 선택이며 최대 500자입니다. 없는 ID는 `404 CATEGORY_NOT_FOUND`입니다.

생성·조회·수정 응답 예시 (`201` 생성, `200` 조회·수정):

```json
{
  "id": 1,
  "name": "학습",
  "description": "학습 관련 게시글"
}
```

현재 Content 모델과 요청 DTO에는 Category 참조가 없습니다. 따라서 게시글에 카테고리를 지정하거나 연결 여부를 검증하는 기능은 아직 구현되어 있지 않습니다.

## Comment API

댓글 생성은 게시글 경로를 사용합니다. 요청의 `userId`는 메모리에 존재하는 User인지 확인하는 학습용 관계 검증 값입니다. 인증된 사용자를 뜻하지 않으며 인증·인가 기능은 구현되어 있지 않습니다.

| 메서드 | URL | 성공 상태 |
|---|---|---|
| POST | `/api/contents/{contentId}/comments` | 201 Created |
| GET | `/api/contents/{contentId}/comments` | 200 OK |
| GET | `/api/comments/{commentId}` | 200 OK |
| PUT | `/api/comments/{commentId}` | 200 OK |
| DELETE | `/api/comments/{commentId}` | 204 No Content |

생성 요청 예시:

```json
{
  "userId": 1,
  "body": "댓글 내용입니다."
}
```

수정 요청은 `body`만 받습니다. 댓글 내용은 필수이며 1,000자 이하입니다. 생성 시 게시글과 User가 모두 존재해야 합니다. 대상이 없으면 각각 `404 CONTENT_NOT_FOUND`, `404 USER_NOT_FOUND`; 없는 댓글 ID는 `404 COMMENT_NOT_FOUND`입니다. 응답 필드는 `id`, `contentId`, `userId`, `body`입니다.

생성·상세·수정 응답 예시 (`201` 생성, `200` 조회·수정):

```json
{
  "id": 1,
  "contentId": 1,
  "userId": 1,
  "body": "댓글 내용입니다."
}
```

게시글 또는 작성자 User를 삭제하면 해당 댓글도 함께 삭제됩니다. 댓글 수정·삭제에는 소유자 확인이 없으므로 인증된 작성자만 변경할 수 있는 API로 사용하면 안 됩니다.

## Postman 테스트

컬렉션 파일: [`postman/Spring-Media-API.postman_collection.json`](postman/Spring-Media-API.postman_collection.json)

1. 애플리케이션을 `http://localhost:8080`에서 실행합니다.
2. Postman에서 **Import**를 눌러 위 JSON 파일을 가져옵니다.
3. 컬렉션 실행 시 폴더 순서를 유지합니다: User → Content → Category → Comment → 관계 및 연쇄 삭제.
4. 생성 요청의 응답에서 ID를 컬렉션 변수에 저장하므로 뒤 요청이 준비 데이터를 사용합니다. 회원 이메일은 매 실행마다 새 값으로 생성합니다.

컬렉션은 정상 CRUD 외에도 Validation 실패, 없는 ID, 중복 이메일, 댓글의 없는 게시글·작성자 참조, 부모 삭제에 따른 댓글 정리를 검사합니다. Content와 Category 관계는 구현되어 있지 않아 연동 요청을 포함하지 않습니다.

## 테스트

전체 테스트:

```bash
./gradlew test
```

Windows PowerShell에서는 `.\gradlew.bat test`를 사용합니다. 테스트는 MockMvc로 API 상태 코드·응답·저장 상태와 관계 검증을 확인합니다.

## 기존 학습 API

- Product: `/api/products`에서 목록·상세·생성·수정·삭제를 제공합니다.
- Media: `GET /api/media`, `GET /api/media/{id}`를 제공합니다.

Media 목록 응답 예시:

```json
[
  "Inception",
  "Interstellar",
  "The Dark Knight"
]
```

`GET /api/media/1` 응답 예시:

```json
{
  "id": 1,
  "title": "Media 1"
}
```

## AOP 로깅

Spring AOP로 서비스 메서드의 실행 전후와 경과 시간을 기록합니다. Aspect는 공통 로깅 기능을, Pointcut은 적용할 메서드를, Advice는 실행할 동작을 정의합니다. Spring은 Bean 프록시를 통해 이를 적용합니다.

`LoggingAspect`의 `@Around` Advice는 `com.example.springstudy.service` 패키지의 메서드를 실행하고 성공·예외 로그를 남깁니다. 예외는 다시 전달하며 요청 값과 반환 객체는 기록하지 않습니다. 의존성은 Spring Boot 4용 `spring-boot-starter-aspectj`입니다.

실행 및 테스트:

```bash
.\gradlew.bat bootRun
.\gradlew.bat test
```

검증 결과: 전체 테스트 `BUILD SUCCESSFUL`; `GET /api/contents`는 `200 OK`와 AOP 시작·종료 로그, 존재하지 않는 ID 조회는 `404 CONTENT_NOT_FOUND`와 AOP 오류 로그를 확인했습니다.

## Proxy 실습

JDK Dynamic Proxy를 직접 구현하고 Spring AOP의 JDK 및 CGLIB 프록시를 비교하는 실습입니다. 클래스별 역할, 호출 흐름, 프록시 확인 방법과 테스트 결과는 [Proxy 실습 노트](docs/proxy-practice.md)를 참고하세요.

## 트랜잭션 실습

테스트 전용 H2 데이터베이스와 Spring JDBC로 트랜잭션 동작을 확인합니다. 실습에서는 커밋과 예외별 롤백, `readOnly`, Self Invocation, 프록시 경계, 전파 옵션, 격리 수준을 다룹니다.

```bash
.\gradlew.bat test --tests "com.example.springstudy.transaction.*"
```

테스트 코드는 `src/test/java/com/example/springstudy/transaction`에 있습니다. 전파·격리 실습의 설명과 결과는 [Transaction 심화 실습 문서](docs/day32-transaction-advanced.md)를 참고하세요.

기본 설정에서 `@Transactional`은 `RuntimeException`과 `Error`가 메서드 밖으로 전달될 때 롤백합니다. Checked Exception도 롤백하려면 `rollbackFor`를 지정해야 합니다. `readOnly = true`는 읽기 전용 힌트이며, DB 쓰기를 항상 차단하지는 않습니다.

### API에서의 적용 범위

회원·콘텐츠·댓글의 쓰기 메서드에는 `@Transactional`이 붙어 있지만, API 저장소는 메모리 기반입니다. 런타임에 JDBC DataSource와 TransactionManager를 사용하지 않으므로 이 어노테이션만으로 메모리 데이터의 롤백이 보장되지는 않습니다.

실제 DB 커밋과 롤백은 테스트 전용 H2 환경의 `TransactionBoundaryService`로 확인합니다. 정상 호출은 주문·항목·이력을 함께 저장하고, 저장 중 예외가 발생하면 세 테이블의 변경을 모두 롤백합니다.
# Day 38 — JPA와 영속성 컨텍스트 실습

이 프로젝트는 Spring Boot 4.1.1, Java 21에서 Spring Data JPA starter가 제공하는 Jakarta Persistence API와 Hibernate 구현체를 사용합니다. `Member` 실습은 기존 API 도메인과 독립되어 있으며 Spring Data Repository를 사용하지 않습니다.

## 핵심 개념

- **ORM**은 객체와 관계형 데이터베이스의 테이블 사이를 매핑해 객체 중심 코드로 데이터를 다루는 방식입니다.
- **JPA**는 Java 객체 관계 매핑과 EntityManager 동작을 정의하는 표준 명세입니다. **Hibernate**는 이 명세를 구현하며 SQL 생성, 상태 추적 등의 동작을 수행합니다.
- **Entity**는 `@Entity`로 매핑한 영속 대상입니다. `Member`는 `members` 테이블의 `id`, `name`, `email` 열에 대응합니다.
- **EntityManager**는 `persist`, `find`, `remove`, `flush` 등을 제공하는 API입니다. Spring의 `@PersistenceContext` 주입을 사용하며 직접 팩토리를 만들지 않습니다.
- **Persistence Context**는 현재 트랜잭션에서 관리하는 Entity를 보관하고 변경 사항을 추적합니다.

```java
Member member = new Member("홍길동", "hong@example.com"); // 비영속
entityManager.persist(member);                              // 영속
member.changeEmail("new@example.com");                     // 변경 감지 대상
entityManager.detach(member);                               // 준영속
```

Entity 생명주기는 비영속(아직 관리되지 않음), 영속(컨텍스트가 관리함), 준영속(detach/clear/close 등으로 관리가 끝남), 삭제(remove 예약) 상태로 설명할 수 있습니다. `detach(entity)`는 한 객체만 분리하고, `clear()`는 컨텍스트의 전체 Entity를 분리하며, `close()`는 EntityManager를 종료합니다.

같은 컨텍스트에서 같은 식별자를 `find`하면 1차 캐시가 보유한 동일 Java 객체를 돌려줍니다. 영속 Entity의 필드 변경은 스냅샷과 비교되어 커밋 전 flush 때 UPDATE로 반영됩니다. `persist` 호출을 모아 두었다가 flush 시 SQL을 내보내는 것을 쓰기 지연이라 합니다. 단, `IDENTITY` 전략은 DB가 키를 생성해야 해서 INSERT가 `persist` 중 실행될 수 있습니다.

**Flush는 영속성 컨텍스트의 SQL을 DB에 보내는 동기화이며 커밋은 아닙니다.** 트랜잭션은 flush 뒤에도 계속 활성화되고 rollback될 수 있습니다. Commit은 트랜잭션의 최종 성공을 확정합니다. 서비스의 예외 테스트는 Spring 트랜잭션 프록시 경계 밖으로 런타임 예외가 전파되어 rollback되는 것을 확인합니다.

## H2 설정과 실습

`application.yml`은 `jdbc:h2:mem:springdb`, `sa`/빈 비밀번호, Hibernate `create-drop`, SQL 로깅/포맷팅을 설정합니다. H2 Console은 `/h2-console`에서 활성화됩니다. 애플리케이션 실행 중 `http://localhost:8080/h2-console`에 접속해 JDBC URL `jdbc:h2:mem:springdb`와 사용자 `sa`를 입력하고 `SELECT * FROM MEMBERS;`로 확인할 수 있습니다. 메모리 DB는 애플리케이션이 종료되면 데이터가 사라집니다.

`MemberService`는 `@PersistenceContext`로 EntityManager를 받아 저장/조회/수정/삭제를 수행합니다. 수정 메서드는 별도 update/save 호출 없이 변경 감지에 맡깁니다. 자세한 검증은 `MemberJpaIntegrationTest`에서 확인합니다.

```powershell
./gradlew test --tests com.example.springstudy.service.MemberJpaIntegrationTest
./gradlew test
```

통합 테스트는 생성/저장, 조회, Dirty Checking, 삭제, 1차 캐시 동일성, `contains` 영속/준영속 판별, flush 후 DB 반영과 트랜잭션 유지, clear의 분리, 프록시 기반 rollback을 검증합니다. 테스트는 성공했으며 SQL 로그에서 Hibernate의 DDL 및 INSERT/SELECT/UPDATE/DELETE를 확인할 수 있습니다. `IDENTITY` INSERT는 키 반환을 위해 persist 시 실행될 수 있고, UPDATE/DELETE 및 나머지 변경 SQL은 flush 시점에 실행됩니다.
