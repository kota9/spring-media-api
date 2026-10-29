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

@SpringBootTest(properties = "spring.aop.proxy-target-class=true")
@ExtendWith(OutputCaptureExtension.class)
class CglibProxyTypeTest {

    @Autowired
    private ProxyDemoService proxyDemoService;

    @Test
    void createsCglibProxyAndRunsAdvice(CapturedOutput output) {
        printProxyDetails();

        String result = proxyDemoService.execute("cglib");

        assertEquals("Executed: cglib", result);
        assertTrue(AopUtils.isAopProxy(proxyDemoService));
        assertTrue(!AopUtils.isJdkDynamicProxy(proxyDemoService));
        assertTrue(AopUtils.isCglibProxy(proxyDemoService));
        assertTrue(output.getAll().contains("[ProxyDemo BEFORE] execute"));
        assertTrue(output.getAll().contains("[ProxyDemo AFTER] execute, elapsed="));
    }

    private void printProxyDetails() {
        System.out.println("Bean class: " + proxyDemoService.getClass().getName());
        System.out.println("Target class: " + AopUtils.getTargetClass(proxyDemoService).getName());
        System.out.println("JDK dynamic proxy: " + AopUtils.isJdkDynamicProxy(proxyDemoService));
        System.out.println("CGLIB proxy: " + AopUtils.isCglibProxy(proxyDemoService));
    }
}
