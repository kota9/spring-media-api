# Spring Media API

## 1. 프로젝트 소개

Spring Boot 기반의 미디어 API 학습 프로젝트입니다.

## 2. 학습 목표

- IoC와 DI 이해
- Spring Bean 및 ApplicationContext 이해
- 계층형 아키텍처 설계
- REST API 구현

## 3. 기술 스택

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Gradle
- Git

## 4. 프로젝트 구조

```text
src/main/java/com/example/springstudy
├── SpringStudyApplication.java
├── controller/
│   └── MediaController.java
├── service/
│   └── MediaService.java
├── repository/
│   └── MediaRepository.java
├── domain/
│   └── Media.java
├── dto/
│   └── MediaResponse.java
└── config/
```

## 5. 실행 방법

### 사전 준비

- JDK 21

### 빌드

Windows:

```bash
gradlew.bat build
```

macOS / Linux:

```bash
./gradlew build
```

### 애플리케이션 실행

Windows:

```bash
gradlew.bat bootRun
```

macOS / Linux:

```bash
./gradlew bootRun
```

## 6. 구현 기능

현재 구현된 API는 미디어 목록 조회와 ID별 미디어 조회입니다. 애플리케이션을 실행한 뒤 기본 주소 `http://localhost:8080`으로 요청합니다.

### 미디어 목록 조회

`GET /api/media`

응답 예시:

```json
[
  "Inception",
  "Interstellar",
  "The Dark Knight"
]
```

### 미디어 단건 조회

`GET /api/media/{id}`

요청 예시:

```http
GET /api/media/1
```

응답 예시:

```json
{
  "id": 1,
  "title": "Media 1"
}
```
