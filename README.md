# Spring Media API

Spring Boot 4 기반의 REST API 구현과 Spring 핵심 동작을 단계별로 실습하는 프로젝트입니다. User, Content, Category, Comment API는 메모리 기반 저장소로 동작하고, JPA Entity 및 트랜잭션 실습은 별도 모델과 H2 데이터베이스를 사용합니다.

## 1. 프로젝트 소개

### 개발 목적

HTTP API 설계부터 Controller–Service–Repository 계층 분리, 입력 검증과 예외 응답까지 구현하고, 같은 프로젝트 안에서 AOP Proxy, Transaction, JPA Persistence Context의 동작을 테스트로 확인합니다.

### 주요 기능

- User, Content, Category, Comment CRUD REST API
- Product CRUD 학습 API와 읽기 전용 Media API
- DTO 기반 요청·응답 및 Jakarta Bean Validation
- 전역 예외 처리와 공통 오류 응답
- Service 메서드 로깅을 위한 Spring AOP
- 메모리 Repository를 사용하는 API와 구분된 JPA Entity / H2 실습
- Spring JDBC 및 H2를 사용한 트랜잭션 경계·전파·격리 실습

### 학습 범위

Spring IoC/DI, Bean과 ApplicationContext, Spring MVC, REST, DTO/Validation, 예외 처리, AOP/Proxy, Transaction, JUnit 5/Mockito/MockMvc, JPA/Hibernate, Entity 생명주기, Persistence Context, Dirty Checking을 다룹니다.

## 2. 학습 목표

- **IoC / DI, Bean, ApplicationContext, Singleton**: Spring 컨테이너가 객체를 생성하고 의존성을 주입하는 구조를 이해합니다.
- **Spring MVC와 REST API**: Controller가 HTTP 요청을 처리하고 Service와 Repository에 책임을 위임하는 흐름을 익힙니다.
- **DTO / Validation**: 도메인 객체와 API 입출력을 분리하고 Jakarta Validation으로 입력을 검증합니다.
- **Exception Handling**: 애플리케이션 예외를 HTTP 상태와 안정적인 오류 코드로 변환합니다.
- **AOP / Proxy**: 공통 로깅 Advice가 Spring Bean Proxy를 통해 호출에 적용되는 방식을 살펴봅니다.
- **Transaction**: Proxy 경계, Commit/Rollback, `rollbackFor`, `readOnly`, Propagation, Isolation, Self Invocation을 테스트합니다.
- **테스트**: JUnit 5와 Mockito로 단위 테스트를, MockMvc로 MVC/API 테스트를 작성하고 H2 기반 통합 실습을 수행합니다.
- **JPA / Hibernate**: EntityManager, Entity 매핑, Persistence Context, 생명주기, Flush와 Dirty Checking을 확인합니다.

## 3. 기술 스택

| 영역 | 실제 사용 기술 |
|---|---|
| 언어 / 빌드 | Java 21, Gradle Wrapper 9.6.0 |
| 애플리케이션 | Spring Boot 4.1.1, Spring Web MVC |
| JSON / 검증 | Jackson, Jakarta Validation |
| AOP / 트랜잭션 | Spring AOP (`spring-boot-starter-aspectj`), Spring Transaction |
| 데이터 접근 | Spring Data JPA starter와 Hibernate, Spring JDBC (테스트) |
| 테스트 DB | H2 (기본 설정 및 테스트 설정) |
| 테스트 | Spring Boot Test, JUnit Platform/JUnit 5, Mockito, AssertJ, MockMvc |

실제 REST API Repository는 Spring Data JPA Repository가 아니라 프로젝트 자체 메모리 구현입니다. JPA 의존성은 별도 EntityManager/Entity 실습에 사용됩니다.

## 4. 프로젝트 아키텍처

```text
src/main/java/com/example/springstudy/
├── SpringStudyApplication.java
├── controller/       # User, Content, Category, Comment, Product, Media HTTP 엔드포인트
├── service/          # API 유스케이스, 검증 및 저장소 호출; MemberService JPA 실습
├── repository/       # 메모리 저장소와 ContentRepository 계약
├── domain/           # API의 일반 도메인 모델 (record 및 Product/Member 모델)
├── entity/           # JPA 실습용 User, Content, Category, Comment Entity와 enum
├── dto/              # API 요청·응답 DTO 및 ApiErrorResponse
├── exception/        # 도메인 예외, ErrorCode, ApiExceptionHandler
├── aop/              # LoggingAspect
└── proxy/            # 수동 JDK Proxy 및 Spring AOP Proxy 실습 코드

src/test/java/com/example/springstudy/
├── controller/       # MockMvc 기반 Controller/API 테스트
├── integration/      # H2 저장소 대체 구성을 사용하는 Content 통합 테스트
├── service/, user/, category/, comment/ # 서비스 및 JPA 서비스 테스트
├── entity/           # JPA 매핑 테스트
├── transaction/      # JDBC 기반 트랜잭션 실습과 테스트 구성
└── proxy/            # JDK/CGLIB 및 Spring Proxy 테스트
```

`controller`는 HTTP 경로·상태 코드와 DTO 변환을 담당하고, `service`는 유스케이스와 관계 검증을 수행합니다. `repository`는 현재 API 데이터 저장을 담당합니다. `domain`과 `entity`는 서로 다른 모델이며, `dto`는 외부 요청/응답 계약입니다. 테스트 전용 JDBC Repository와 트랜잭션 구성은 `src/test` 아래에 있습니다.

## 5. 실행 방법

JDK 21이 필요합니다. 애플리케이션 기본 포트는 Spring 기본값인 **8080**이며 별도 포트 재정의는 `application.yml`에 없습니다. 기본 프로파일은 H2 메모리 DB(`jdbc:h2:mem:springdb`)와 H2 Console(`/h2-console`)을 설정합니다. REST CRUD 데이터 자체는 메모리 Repository에 저장됩니다.

### Windows

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
.\gradlew.bat bootRun
```

### macOS / Linux

```bash
./gradlew clean build
./gradlew test
./gradlew bootRun
```

API 기본 주소: `http://localhost:8080`. JSON 요청에는 `Content-Type: application/json`을 지정합니다.

## 6. 주요 기능 및 API 명세

요청/응답 DTO 표기는 아래에서 `Request → Response`로 나타냅니다. 경로의 식별자는 실제 Controller 경로 변수입니다. 생성 API는 `201 Created`를 반환하며 User, Content, Category, Comment는 생성 리소스를 가리키는 `Location` 헤더도 반환합니다. Product는 201이지만 `Location`을 설정하지 않습니다.

| 리소스 | Method 및 URL | 기능 | 성공 | 요청 → 응답 DTO | 주요 검증 / 오류 |
|---|---|---|---|---|---|
| User | `POST /api/users` | 회원 생성 | 201 + Location | `UserRequest → UserResponse` | name 필수 2~20자, email 필수·형식, 중복 email 409 `DUPLICATE_USER_EMAIL`, 입력 오류 400 `INVALID_REQUEST` |
| User | `GET /api/users`, `GET /api/users/{userId}` | 목록·상세 조회 | 200 | 없음 → `UserResponse[]` / `UserResponse` | 미존재 ID 404 `USER_NOT_FOUND` |
| User | `PUT /api/users/{userId}` | 전체 필드 갱신 | 200 | `UserRequest → UserResponse` | 생성과 같은 검증, 미존재 404, 중복 email 409 |
| User | `DELETE /api/users/{userId}` | 삭제 및 연결 댓글 정리 | 204 | 없음 | 미존재 404 `USER_NOT_FOUND` |
| Content | `POST /api/contents` | 게시물 생성 | 201 + Location | `ContentCreateRequest → ContentResponse` | title 필수·최대 100자, description 필수, 400 `INVALID_REQUEST` |
| Content | `GET /api/contents`, `GET /api/contents/{id}` | 목록·상세 조회 | 200 | 없음 → `ContentResponse[]` / `ContentResponse` | 미존재 ID 404 `CONTENT_NOT_FOUND` |
| Content | `PUT /api/contents/{id}` | title과 description 전체 갱신 | 200 | `ContentUpdateRequest → ContentResponse` | title 필수·최대 100자, description 필수, 미존재 404 |
| Content | `PATCH /api/contents/{id}` | 전달된 필드만 변경 | 200 | `ContentPatchRequest → ContentResponse` | 변경 필드 1개 이상, 전달 필드 null/공백/부적합 값이면 400 `INVALID_CONTENT_PATCH`, 미존재 404 |
| Content | `DELETE /api/contents/{id}` | 삭제 및 연결 댓글 정리 | 204 | 없음 | 미존재 404 `CONTENT_NOT_FOUND` |
| Category | `POST /api/categories` | 분류 생성 | 201 + Location | `CategoryRequest → CategoryResponse` | name 필수·최대 100자, description 선택·최대 500자, 입력 오류 400 |
| Category | `GET /api/categories`, `GET /api/categories/{categoryId}` | 목록·상세 조회 | 200 | 없음 → `CategoryResponse[]` / `CategoryResponse` | 미존재 404 `CATEGORY_NOT_FOUND` |
| Category | `PUT /api/categories/{categoryId}` | 전체 갱신 | 200 | `CategoryRequest → CategoryResponse` | 생성과 같은 검증, 미존재 404 |
| Category | `DELETE /api/categories/{categoryId}` | 삭제 | 204 | 없음 | 미존재 404 `CATEGORY_NOT_FOUND` |
| Comment | `POST /api/contents/{contentId}/comments` | 댓글 생성 | 201 + Location | `CommentCreateRequest → CommentResponse` | `userId` 필수, body 필수·최대 1,000자; Content/User 부재 시 각각 404 |
| Comment | `GET /api/contents/{contentId}/comments` | 게시물별 댓글 조회 | 200 | 없음 → `CommentResponse[]` | Content 부재 404 `CONTENT_NOT_FOUND` |
| Comment | `GET /api/comments/{commentId}` | 상세 조회 | 200 | 없음 → `CommentResponse` | 404 `COMMENT_NOT_FOUND` |
| Comment | `PUT /api/comments/{commentId}` | body 전체 갱신 | 200 | `CommentUpdateRequest → CommentResponse` | body 필수·최대 1,000자, 댓글 부재 404 |
| Comment | `DELETE /api/comments/{commentId}` | 삭제 | 204 | 없음 | 댓글 부재 404 `COMMENT_NOT_FOUND` |
| Product | `POST /api/products` | 상품 생성 | 201 | `ProductRequest → ProductResponse` | name 비공백, price 양수, 입력 오류 400 `INVALID_REQUEST` |
| Product | `GET /api/products`, `GET /api/products/{id}` | 목록·상세 조회 | 200 | 없음 → `ProductResponse[]` / `ProductResponse` | 조회 API에 별도 Not Found 매핑은 없음 |
| Product | `PUT /api/products/{id}` | 상품 전체 갱신 | 200 | `ProductRequest → ProductResponse` | name 비공백, price 양수 |
| Product | `DELETE /api/products/{id}` | 삭제 | 204 | 없음 | 별도 Not Found 오류 매핑은 없음 |
| Media | `GET /api/media` | 고정 미디어 제목 목록 조회 | 200 | 없음 → 문자열 배열 | 고정 목록을 반환 |
| Media | `GET /api/media/{id}` | 미디어 상세 예시 조회 | 200 | 없음 → Map(`id`, `title`) | ID와 무관하게 `Media {id}` 형태 응답을 만듦; 없는 ID에 404를 반환하지 않음 |

Content의 `PUT`은 DTO의 두 필드를 모두 요구하는 전체 갱신이고 `PATCH`는 전달한 필드만 부분 변경합니다. `PATCH`는 Bean Validation이 아니라 명시적 검사로 잘못된 요청을 `INVALID_CONTENT_PATCH` 처리합니다. 댓글 생성 시 Service가 Content와 User 존재를 각각 검증합니다. REST API에서 User 또는 Content를 삭제하면 해당 메모리 Repository의 댓글도 함께 제거합니다. Content와 Category 간 연결은 REST API에 구현되어 있지 않습니다.

### 요청 예시

```json
{
  "name": "홍길동",
  "email": "hong@example.com"
}
```

```json
{
  "title": "Spring 학습 기록",
  "description": "게시물 내용"
}
```

```json
{
  "userId": 1,
  "body": "댓글 내용"
}
```

## 7. 공통 응답 및 예외 처리

`ApiExceptionHandler`는 `@RestControllerAdvice`로 예외를 다음 구조로 변환합니다. `timestamp`는 서버 생성 시각이며 `path`는 요청 URI입니다.

```json
{
  "timestamp": "2026-01-01T12:00:00",
  "status": 404,
  "code": "CONTENT_NOT_FOUND",
  "message": "요청한 콘텐츠를 찾을 수 없습니다.",
  "path": "/api/contents/999"
}
```

| HTTP 상태 | 코드 | 처리 내용 |
|---:|---|---|
| 400 | `INVALID_REQUEST` | Bean Validation 실패, 잘못된 JSON/본문, ConstraintViolationException. ResponseStatusException의 4xx도 이 코드로 응답 |
| 400 | `INVALID_CONTENT_PATCH` | 비어 있거나 유효하지 않은 Content PATCH 필드 |
| 404 | `USER_NOT_FOUND` | User 부재 |
| 404 | `CONTENT_NOT_FOUND` | Content 부재 |
| 404 | `CATEGORY_NOT_FOUND` | Category 부재 |
| 404 | `COMMENT_NOT_FOUND` | Comment 부재 |
| 409 | `DUPLICATE_USER_EMAIL` | 이미 사용 중인 이메일 |
| 500 | `INTERNAL_SERVER_ERROR` | 처리되지 않은 예외. 서버 로그에 예외 기록 |

## 8. 저장소 및 데이터 관리 방식

### REST API 학습 영역

User, Content, Category, Comment 및 Product 데이터는 `ConcurrentHashMap` 등 메모리 Repository에 저장됩니다. Media는 고정 목록을 반환합니다. 애플리케이션을 재시작하면 변경 가능한 메모리 데이터는 초기화됩니다. 댓글 생성 시 User/Content 존재 검증과 User/Content 삭제 시 댓글 정리는 Service에서 수행합니다.

이 영역의 Service 일부에 `@Transactional`이 있어도 메모리 자료구조 변경을 데이터베이스처럼 자동 롤백하지 않습니다. 실제 영속성 트랜잭션은 JDBC/JPA 리소스와 TransactionManager 경계에 적용됩니다.

### JPA 및 H2 학습 영역

`MemberService`는 `EntityManager`로 `Member`를 저장·조회·수정·삭제합니다. `src/main/resources/application.yml`은 H2 `jdbc:h2:mem:springdb`와 Hibernate `create-drop`을 설정합니다. `MemberJpaIntegrationTest`, `MemberPersistenceContextTest`, `EntityMappingTest`는 Spring Boot 설정의 H2와 JPA를 사용합니다.

트랜잭션 패키지 테스트는 `src/test/resources`의 테스트별 SQL 스키마와 테스트 구성을 통해 Spring JDBC 및 H2를 사용합니다. `ContentIntegrationTest`는 `application-test.yml`의 `jdbc:h2:mem:content-integration`을 사용하고, 테스트 안의 `@Primary` JDBC Repository로 Content 저장소를 대체해 DB 저장 및 테스트 트랜잭션 롤백을 확인합니다. 이는 운영 REST API의 기본 메모리 Repository가 H2를 사용한다는 뜻은 아닙니다.

## 9. Spring AOP / Proxy 실습

AOP는 여러 Service에 반복되는 로깅 같은 횡단 관심사를 핵심 로직과 분리합니다. Aspect는 적용 관심사, Pointcut은 대상 호출, Advice는 실행 시점과 동작을 정의합니다. `LoggingAspect`의 `@Around`는 `execution(* com.example.springstudy.service..*(..))`에 맞는 Service 패키지 메서드 호출 전후를 감싸며 시작, 정상 종료 및 경과 시간, 예외 종류·메시지를 기록한 뒤 예외를 다시 던집니다.

Spring AOP는 Bean을 Proxy로 감싸 호출을 가로챕니다. 인터페이스가 있으면 JDK Dynamic Proxy, 클래스 기반이면 CGLIB Proxy가 사용될 수 있습니다. 프로젝트에는 수동 JDK Proxy와 Spring Proxy 타입을 확인하는 실습 코드/테스트가 있습니다. 실제 선택은 Spring 설정과 대상 타입에 따릅니다.

- 정상 로그 형식: `[AOP START] ...`, `[AOP END] ... elapsed=...ms`
- 예외 로그 형식: `[AOP ERROR] ... exception=...`; 원래 예외를 호출자에게 전달
- 문서: [Proxy 실습](docs/proxy-practice.md)

## 10. Transaction 실습

`@Transactional`은 Spring AOP Proxy가 경계에서 트랜잭션을 시작·완료하도록 합니다. 정상 반환 시 Commit, 기본 설정에서 `RuntimeException` 또는 `Error`가 경계를 벗어나면 Rollback됩니다. Checked Exception은 기본적으로 Rollback 대상이 아니며 `rollbackFor`로 지정할 수 있습니다. `readOnly`는 읽기 전용 의도를 전달하는 힌트이며 모든 DB 변경을 차단하는 보장은 아닙니다.

Propagation은 기존 트랜잭션 참여/새 트랜잭션 생성 규칙을, Isolation은 동시 트랜잭션에서 데이터 가시성 수준을 정합니다. Self Invocation으로 같은 객체 내부에서 메서드를 직접 호출하면 Proxy를 통과하지 않아 내부 메서드의 트랜잭션 설정이 별도로 적용되지 않을 수 있습니다. 별도 Bean을 통한 호출과 Proxy 경계 테스트가 포함되어 있습니다.

API `UserService`는 create/update/delete, `ContentService`는 create/update/patch/delete, `CommentService`는 create/update/delete에 `@Transactional`이 선언되어 있습니다. 이 서비스들이 변경하는 API 데이터는 메모리 저장소이므로 선언만으로 그 변경이 DB처럼 롤백되지는 않습니다. 실제 커밋·롤백과 JDBC 리소스 검증은 테스트 전용 H2와 Spring JDBC를 사용하는 실습에서 수행합니다. 해당 테스트에는 Transaction Proxy, 경계, `rollbackFor`, `readOnly`, Propagation, Isolation, Self Invocation 사례가 있습니다.

심화 자료: [Transaction 심화 실습](docs/day32-transaction-advanced.md). 관련 코드는 [transaction 테스트 패키지](src/test/java/com/example/springstudy/transaction/)에 있습니다.

## 11. JPA 실습

### 기본 구성과 Entity 설계

ORM은 객체와 관계형 테이블 사이의 매핑 방식이고, JPA는 Java ORM 표준, Hibernate는 프로젝트의 JPA 구현체입니다. `MemberService`는 `EntityManager`를 통해 Entity를 저장·조회하며 트랜잭션 동안 Persistence Context가 Entity 상태를 관리합니다.

`entity` 패키지의 JPA 모델은 API의 `domain` 모델과 별개입니다. User, Content, Category, Comment Entity가 있으며 `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@Enumerated`, `@ManyToOne`, `@JoinColumn`을 사용합니다. 연관관계는 LAZY로 매핑되어 있습니다. JPA `Content`는 author 및 Category와 연결되고, JPA `Comment`는 author 및 Content와 연결됩니다. 특히 JPA Content–Category 연관 매핑은 존재하지만 REST Content DTO/도메인에는 Category 참조가 없고 API 연결 기능도 제공하지 않습니다.

### Entity 생명주기와 Persistence Context

- **비영속(Transient)**: 생성했지만 EntityManager에 등록되지 않은 객체
- **영속(Managed)**: `persist` 또는 조회로 Persistence Context가 관리하는 객체
- **준영속(Detached)**: `detach`/`clear` 등으로 관리에서 분리된 객체
- **삭제(Removed)**: `remove`로 삭제 예약된 객체

Persistence Context는 1차 캐시를 통해 같은 트랜잭션에서 같은 식별자의 객체 동일성을 보장하고, SQL 실행을 Flush 시점까지 늦출 수 있습니다. 관리 상태 Entity의 변경을 추적하는 Dirty Checking으로 UPDATE를 반영합니다.

### Dirty Checking, Flush, Commit

Hibernate는 관리 대상의 로딩 시점 상태 스냅샷과 현재 값을 비교해 변경을 찾고, Flush 시점에 필요한 SQL을 DB에 보냅니다. Commit은 트랜잭션을 확정합니다. Flush는 SQL 동기화이지 Commit 자체가 아니므로 Flush 후에도 트랜잭션은 계속될 수 있고 이후 Rollback될 수 있습니다. `MemberPersistenceContextTest`와 `MemberJpaIntegrationTest`가 1차 캐시, detach, Dirty Checking, Flush 및 롤백을 검증합니다. Entity 매핑 검증은 `EntityMappingTest`에 있습니다.

학습 노트: [Persistence Context 실습](docs/day39-persistence-context.md).

## 12. 테스트 및 Postman

### 테스트 구성

- JUnit 5 및 Spring Boot Test 기반 테스트
- Mockito를 이용한 Service 단위 테스트
- MockMvc 기반 Controller/API 검증: `controller` 테스트 패키지
- Service 테스트: `user`, `category`, `comment`, `service` 패키지
- H2/JPA 통합 테스트: `MemberJpaIntegrationTest`, `MemberPersistenceContextTest`, `EntityMappingTest`
- JDBC/H2 트랜잭션 테스트: `transaction` 패키지
- 테스트에서 JDBC Repository를 연결하는 Content 통합 테스트: `integration/ContentIntegrationTest`

전체 테스트 실행:

```powershell
.\gradlew.bat test
```

```bash
./gradlew test
```

### Postman Collection

Collection: [Spring-Media-API.postman_collection.json](postman/Spring-Media-API.postman_collection.json). `baseUrl` 기본값은 `http://localhost:8080`이며 User, Content, Category, Comment 흐름과 입력 검증 실패, 없는 ID, 중복 이메일 및 부모 삭제 후 댓글 정리 시나리오를 포함합니다. 생성한 ID를 Collection 변수에 저장하도록 구성되어 있어 Collection의 요청 순서에 따라 실행할 수 있습니다. Postman 실행은 앱을 별도로 실행한 상태에서 진행합니다.

## 13. 학습 문서 링크

- [Proxy 실습 문서](docs/proxy-practice.md)
- [Transaction 심화 실습 문서](docs/day32-transaction-advanced.md)
- [Persistence Context 실습 문서](docs/day39-persistence-context.md)
- [Transaction 내부 동작 문서](docs/day33-transaction-internal.md)

## 14. 향후 개선 사항

아래 항목은 현재 구현 범위가 아니라 확장 아이디어입니다.

- REST API Repository를 Spring Data JPA 기반 영속 저장소로 전환
- H2 외에 MySQL 등 운영형 데이터베이스 연결
- API의 User, Content, Category 및 Comment 모델 관계를 명시적 연관관계로 적용
- 목록 API 페이지네이션, 정렬 및 검색
- 인증 및 인가와 작성자 소유권 검증
- 실제 DB를 사용하는 API 통합 테스트 확대
- OpenAPI 기반 API 문서 자동화
- Docker 실행 환경 구성
