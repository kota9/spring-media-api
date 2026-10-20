# spring-media-api

Spring Boot 기반 미디어 REST API 학습 프로젝트입니다.

## 패키지 구조

기본 패키지는 `com.example.springstudy`입니다. Spring Boot 애플리케이션 클래스도 이 패키지 바로 아래에 둡니다.

```text
com.example.springstudy
├── SpringStudyApplication.java
├── controller/
│   └── MediaController.java
├── service/
├── repository/
├── domain/
├── dto/
│   ├── request/                # API 요청 형식
│   └── response/               # API 응답 형식
├── config/
└── exception/
```

### 패키지 책임

| 패키지 | 책임 |
| --- | --- |
| `controller` | HTTP 경로와 요청·응답을 처리하고 서비스에 작업을 위임합니다. |
| `service` | 미디어 관련 애플리케이션 규칙을 처리하고 필요한 경우 트랜잭션 경계를 둡니다. |
| `repository` | 저장소에서 미디어 데이터를 조회·저장합니다. |
| `domain` | 미디어의 핵심 데이터와 상태를 표현합니다. |
| `dto.request` | 클라이언트가 보내는 요청 필드와 검증 규칙을 표현합니다. |
| `dto.response` | API가 반환하는 필드를 표현합니다. 도메인 객체를 그대로 외부에 노출하지 않습니다. |
| `config` | 보안, 직렬화 등 기본 자동 설정만으로 해결되지 않는 애플리케이션 설정을 둡니다. |
| `exception` | 공통 예외와 API 오류 응답 처리 코드를 둡니다. |

## 미디어 API 파일 예시

현재 실제 파일은 `SpringStudyApplication`과 간단한 하드코딩 응답을 반환하는 `MediaController`입니다. 지금 동작을 유지하기 위해 추가 클래스는 필요하지 않습니다. 기능을 확장할 때 아래 순서로 필요한 파일만 추가합니다.

```text
controller/MediaController.java                 # 현재 존재: HTTP 진입점
service/MediaService.java                       # 조회·등록 규칙이 생길 때
dto/request/MediaCreateRequest.java             # 등록 API를 만들 때
dto/response/MediaResponse.java                 # 응답 형식을 고정할 때
domain/Media.java                               # 미디어 데이터를 도메인 모델로 관리할 때
repository/MediaRepository.java                 # DB 등 영속 저장소를 연결할 때
exception/ApiExceptionHandler.java              # 일관된 오류 응답이 필요할 때
config/                                        # 별도 설정이 생길 때만 클래스 추가
```

예를 들어 현재의 메모리 기반 조회 API를 서비스 계층으로 옮긴다면 `MediaService`와 `MediaResponse`를 우선 추가할 수 있습니다. 데이터베이스 저장을 구현할 때 `Media`, `MediaRepository`, 필요한 DB 설정을 함께 도입합니다. 등록 기능 전에는 `MediaCreateRequest`를 만들지 않습니다. 별도 설정이나 공통 오류 응답이 아직 없다면 `config`, `exception`에는 빈 클래스나 자리 표시자 클래스를 두지 않습니다.

## 의존 관계

일반적인 요청 흐름은 다음과 같습니다.

```text
HTTP 요청 → MediaController → MediaService → MediaRepository → 저장소
                 ↓                 ↓
          Request/Response DTO   Domain
```

`Controller`는 DTO를 받아 서비스 호출 결과를 DTO로 반환합니다. `Service`는 도메인 규칙을 처리하고 저장이 필요할 때 `Repository`를 사용합니다. `Repository`는 도메인 모델을 저장소와 연결합니다. `Domain`은 DTO나 웹 계층에 의존하지 않습니다. `config`와 `exception`은 필요할 때 애플리케이션 전반에 적용되는 설정과 오류 응답을 제공합니다.

현재처럼 하드코딩된 응답만 있는 단계에서는 `Controller`만 존재해도 됩니다. 영속성이 없는 서비스 단계라면 `Repository`와 `Domain`은 아직 필요하지 않습니다.

## 컴포넌트 스캔

`SpringStudyApplication`은 기본 패키지 `com.example.springstudy`에 있습니다. `@SpringBootApplication`의 기본 컴포넌트 스캔은 이 패키지와 하위 패키지를 대상으로 하므로, 위 구조의 컨트롤러와 향후 서비스·저장소 구현을 별도 스캔 설정 없이 검색할 수 있습니다. 애플리케이션 클래스를 하위 패키지로 옮기면 다른 패키지가 스캔 범위에서 빠질 수 있으므로 기본 패키지에 유지합니다.

## 향후 확장

- 미디어 검색·등록·수정 요구사항이 정해지면 필요한 요청/응답 DTO와 서비스 메서드를 추가합니다.
- DB를 선택한 뒤 영속화 방식에 맞춰 도메인과 저장소를 도입합니다. JPA를 선택한다면 그때 엔티티와 Spring Data 저장소를 구성합니다.
- 입력 검증과 공통 오류 응답 규격이 필요해질 때 검증 어노테이션과 예외 처리기를 추가합니다.
- 인증, 외부 미디어 저장소, API 문서 설정은 해당 기능이 요구될 때 각각 구성합니다.

## Git Commit 규칙

커밋 메시지는 변경 목적이 드러나도록 작성합니다.

| 유형 | 용도 | 예시 |
| --- | --- | --- |
| `feat` | 새 기능 추가 | `feat: 미디어 등록 API 추가` |
| `fix` | 버그 수정 | `fix: 미디어 조회 오류 수정` |
| `refactor` | 기능 변경 없는 구조 개선 | `refactor: 서비스 책임 분리` |
| `test` | 테스트 추가 및 수정 | `test: 미디어 서비스 테스트 추가` |
| `docs` | 문서 변경 | `docs: 프로젝트 README 작성` |
| `chore` | 설정 및 관리 작업 | `chore: 초기 프로젝트 설정` |
