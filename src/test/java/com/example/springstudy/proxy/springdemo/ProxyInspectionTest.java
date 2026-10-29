package com.example.springstudy.proxy.springdemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ProxyInspectionTest {

    @Autowired
    private ProxyDemoService proxyDemoService;

    @Test
    void inspectSpringProxyAndInvokeAdvice(CapturedOutput output) {
        boolean aopProxy = AopUtils.isAopProxy(proxyDemoService);
        boolean jdkDynamicProxy = AopUtils.isJdkDynamicProxy(proxyDemoService);
        boolean cglibProxy = AopUtils.isCglibProxy(proxyDemoService);
        Class<?> targetClass = AopUtils.getTargetClass(proxyDemoService);

        System.out.println("Bean class: " + proxyDemoService.getClass().getName());
        System.out.println("AOP proxy: " + aopProxy);
        System.out.println("JDK dynamic proxy: " + jdkDynamicProxy);
        System.out.println("CGLIB proxy: " + cglibProxy);
        System.out.println("Target class: " + targetClass.getName());

        String result = proxyDemoService.execute("inspection");

        assertEquals("Executed: inspection", result);
        assertTrue(aopProxy, "Injected service should be an AOP proxy");
        assertTrue(jdkDynamicProxy || cglibProxy, "Proxy should be JDK or CGLIB based");
        assertEquals(ProxyDemoServiceImpl.class, targetClass);
        assertTrue(output.getAll().contains("[ProxyDemo BEFORE] execute"));
        assertTrue(output.getAll().contains("[ProxyDemo AFTER] execute, elapsed="));
    }
}
