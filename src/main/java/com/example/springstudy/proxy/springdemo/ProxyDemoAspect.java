package com.example.springstudy.proxy.springdemo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProxyDemoAspect {
    private static final Logger log = LoggerFactory.getLogger(ProxyDemoAspect.class);

    @Around("execution(* com.example.springstudy.proxy.springdemo.ProxyDemoServiceImpl.execute(..))")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        log.info("[ProxyDemo BEFORE] execute");
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("[ProxyDemo AFTER] execute, elapsed={}ms", elapsedMillis);
        }
    }
}
