package com.example.springstudy.proxy.springdemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ProxyDemoSpringAopTest {

    @Autowired
    private ProxyDemoService proxyDemoService;

    @Test
    void springProxyRunsAdviceAndReturnsTargetResult(CapturedOutput output) {
        String result = proxyDemoService.execute("hello");

        assertEquals("Executed: hello", result);
        assertTrue(output.getAll().contains("[ProxyDemo BEFORE] execute"));
        assertTrue(output.getAll().contains("[ProxyDemo AFTER] execute, elapsed="));
    }
}
