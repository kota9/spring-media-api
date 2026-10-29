# Proxy 실습 노트

## 개념

Proxy(프록시)는 Target 객체 호출을 대신 받아 로깅, 실행 시간 측정 같은 부가 동작을 수행한 뒤 Target에 위임합니다. 프록시를 우회해 Target을 직접 호출하면 해당 프록시의 Advice는 실행되지 않습니다.

### JDK Dynamic Proxy

Java 표준 `java.lang.reflect.Proxy`와 `InvocationHandler`로 런타임에 생성하며, 대상 클래스는 인터페이스를 구현해야 합니다. `JdkDynamicProxyTest`는 `Proxy.newProxyInstance()`로 프록시를 만들고 실행 전 로그, Target 위임, 실행 후 로그와 반환값을 검증합니다. `Method.invoke()`가 Target 예외를 `InvocationTargetException`으로 감싸므로 원인 예외를 다시 전달합니다.

### CGLIB

CGLIB 기반 프록시는 대상 클래스를 상속해 생성합니다. 따라서 대상 클래스와 Advice 적용 메서드는 `final`일 수 없습니다.

### Spring AOP

Spring AOP는 컨테이너의 Bean 호출에 Proxy를 적용해 Advice를 실행합니다. `ProxyDemoAspect`는 다음 메서드에만 적용되어 기존 서비스 로깅 Aspect와 겹치지 않습니다.

```java
execution(* com.example.springstudy.proxy.springdemo.ProxyDemoServiceImpl.execute(..))
```

Advice는 `try-finally`에서 종료 로그와 실행 시간을 기록하므로 Target이 예외를 던져도 종료 기록이 수행됩니다. 프로젝트는 Spring Boot 4의 `spring-boot-starter-aspectj` 의존성을 사용합니다.

## 프록시 확인

Spring Bean에 다음 정보를 출력해 프록시와 실제 Target 클래스를 확인할 수 있습니다.

```java
bean.getClass().getName();
AopUtils.isAopProxy(bean);
AopUtils.isJdkDynamicProxy(bean);
AopUtils.isCglibProxy(bean);
AopUtils.getTargetClass(bean).getName();
```

프록시 구현 클래스명은 Spring 버전과 실행 환경에 따라 달라질 수 있으므로 클래스명을 고정해 테스트하지 않고 `AopUtils`의 결과를 확인합니다.

## JDK와 CGLIB 비교

| 설정 | 생성 방식 | 실행 시 프록시 예시 | Target |
|---|---|---|---|
| `spring.aop.proxy-target-class=false` | 인터페이스 기반 JDK Proxy | `jdk.proxy3.$Proxy158` | `ProxyDemoServiceImpl` |
| `spring.aop.proxy-target-class=true` | Target 상속 기반 CGLIB | `ProxyDemoServiceImpl$$SpringCGLIB$$0` | `ProxyDemoServiceImpl` |

두 테스트 모두 `ProxyDemoService` 인터페이스로 Bean을 주입받습니다. 이 프로젝트 실행에서 JDK 설정은 `isJdkDynamicProxy=true`, CGLIB 설정은 `isCglibProxy=true`였고, 두 프록시 모두 Advice 로그와 반환값 검증에 성공했습니다. 예시 클래스명은 실행 결과일 뿐 고정값이 아닙니다.

각 프록시 유형 테스트는 `@SpringBootTest(properties = ...)`로 별도 속성을 사용합니다. 전역 `application.yml`을 수정하지 않았으며 프로젝트에서 `@EnableAspectJAutoProxy` 설정은 발견되지 않았습니다.

## 실습 클래스

- JDK Proxy: `proxy.jdk.MessageService`, `MessageServiceImpl`, `JdkDynamicProxyTest`
- Spring AOP: `proxy.springdemo.ProxyDemoService`, `ProxyDemoServiceImpl`, `ProxyDemoAspect`
- Spring AOP 테스트: `ProxyDemoSpringAopTest`, `ProxyInspectionTest`, `JDKProxyTypeTest`, `CglibProxyTypeTest`

## 주의사항 및 테스트

- JDK 동적 프록시 대상은 인터페이스를 구현해야 합니다.
- Spring AOP Advice는 컨테이너에서 주입받은 Bean 호출 경로에 적용됩니다.
- Pointcut을 구체적으로 지정하면 기존 Aspect와의 중복을 피할 수 있습니다.
- 프록시 방식 비교는 테스트 속성으로 분리해 전역 설정에 영향을 주지 않도록 합니다.

전체 검증 명령:

```bash
.\gradlew.bat test
```

2026-10-29 실행 결과: `BUILD SUCCESSFUL`, 총 24개 테스트 통과, 실패 0개.
