# Spring Media API

Spring Boot와 Java로 구현한 REST API 프로젝트입니다. 사용자, 게시글, 카테고리, 댓글 API를 제공하고, JPA 기반 데이터 저장과 요청 검증, 트랜잭션 처리를 적용했습니다.

## 기술 스택

- Java 21, Spring Boot 4.1.1
- Spring MVC, Spring Data JPA, Hibernate
- H2 기본 데이터베이스, MySQL 프로파일
- Jakarta Validation
- JUnit 5, Mockito, MockMvc, AssertJ

## 주요 기능

- 사용자, 게시글, 카테고리, 댓글의 REST API
- 게시글 작성 시 작성자와 카테고리 연결, 댓글 작성 시 게시글과 사용자 연결
- DTO와 Bean Validation을 이용한 요청 검증
- 공통 오류 응답과 리소스별 404 처리
- PBKDF2 기반 비밀번호 해시 저장
- 서비스 계층의 트랜잭션 처리와 AOP 요청 로깅
- JPA 연관관계, 영속성 컨텍스트, 변경 감지 실습

`Product` API는 메모리 저장소를 사용하는 CRUD 예제이며, `Media` API는 간단한 조회 예제입니다.

## API

| 리소스 | 주요 경로 |
| --- | --- |
| 사용자 | `/api/users` |
| 게시글 | `/api/contents` |
| 카테고리 | `/api/categories` |
| 댓글 | `/api/contents/{contentId}/comments`, `/api/comments/{commentId}` |
| 상품 | `/api/products` |
| 미디어 예제 | `/api/media` |

각 리소스의 생성, 조회, 수정, 삭제 지원 범위는 Controller에 구현되어 있습니다. 게시글 생성 요청에는 `title`, `description`, `authorId`, `categoryId`가 필요합니다. 댓글 생성 요청에는 `userId`와 `body`가 필요합니다.

```json
{
  "title": "Spring Boot 학습 기록",
  "description": "REST API 게시글 내용",
  "authorId": 1,
  "categoryId": 1
}
```

## 실행

Java 21이 필요합니다. 기본 설정은 H2 인메모리 데이터베이스를 사용합니다.

```powershell
.\gradlew.bat bootRun
```

macOS 또는 Linux:

```bash
./gradlew bootRun
```

서버 주소는 `http://localhost:8080`입니다. 기본 설정에서는 H2 콘솔을 `http://localhost:8080/h2-console`에서 사용할 수 있습니다.

### MySQL 프로파일

`application-mysql.yml`에 MySQL 연결 설정이 있습니다. `DB_USERNAME`, `DB_PASSWORD` 환경 변수를 설정하고 `mysql` 프로파일로 실행합니다.

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_USERNAME = "springstudy"
$env:DB_PASSWORD = "your-password"
.\gradlew.bat bootRun
```

## 테스트

```powershell
.\gradlew.bat test
```

macOS 또는 Linux:

```bash
./gradlew test
```

테스트는 서비스 단위 테스트, MockMvc 기반 컨트롤러 테스트, H2/JPA 매핑 및 통합 테스트를 포함합니다.

## API 요청 예제

`users.http`, `contents.http`, `comments.http` 파일에서 IntelliJ HTTP Client 요청을 실행할 수 있습니다. 게시글과 댓글 예제는 필요한 사용자·카테고리·게시글을 먼저 만들고 응답에서 받은 ID를 다음 요청에 사용합니다. 전체 시나리오는 각 파일의 **Run All Requests in File**로 실행합니다.

Postman 컬렉션: [Spring-Media-API.postman_collection.json](postman/Spring-Media-API.postman_collection.json)

## 프로젝트 구조

```text
src/main/java/com/example/springstudy/
├── controller/  # HTTP 엔드포인트
├── service/     # 비즈니스 로직과 트랜잭션
├── repository/  # 데이터 접근
├── entity/      # JPA 엔티티
├── domain/      # API 도메인 모델
├── dto/         # 요청·응답 모델
└── exception/   # 오류 코드와 공통 예외 응답
```
