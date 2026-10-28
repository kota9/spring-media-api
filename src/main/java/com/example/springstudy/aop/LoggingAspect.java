package com.example.springstudy.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.springstudy.service..*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName() + "(..)";
        long startNanos = System.nanoTime();
        log.info("[AOP START] method={}", method);

        try {
            Object result = joinPoint.proceed();
            log.info("[AOP END] method={}, elapsed={}ms", method, elapsedMillis(startNanos));
            return result;
        } catch (Throwable exception) {
            log.error("[AOP ERROR] method={}, elapsed={}ms", method, elapsedMillis(startNanos), exception);
            throw exception;
        }
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
