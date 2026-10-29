package com.example.springstudy.proxy.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class JdkDynamicProxyTest {

    @Test
    void proxyReturnsTargetResultAndLogsAroundInvocation() {
        MessageService target = new MessageServiceImpl();
        MessageService proxy = createLoggingProxy(target);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        String result;
        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOut);
            result = proxy.send("hello");
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("Processed message: hello", result);
        String logs = output.toString(StandardCharsets.UTF_8);
        assertTrue(logs.contains("[Proxy BEFORE] send"));
        assertTrue(logs.contains("[Proxy AFTER] send"));
        assertTrue(logs.indexOf("[Proxy BEFORE]") < logs.indexOf("[Proxy AFTER]"));
    }

    @Test
    void directTargetCallDoesNotRunProxyLogs() {
        MessageService target = new MessageServiceImpl();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        String result;
        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOut);
            result = target.send("direct");
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("Processed message: direct", result);
        assertEquals("", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void proxyPropagatesOriginalTargetException() {
        IllegalArgumentException targetException = new IllegalArgumentException("target failure");
        MessageService target = message -> {
            throw targetException;
        };
        MessageService proxy = createLoggingProxy(target);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> proxy.send("hello"));

        assertEquals(targetException, thrown);
    }

    private MessageService createLoggingProxy(MessageService target) {
        InvocationHandler handler = (proxy, method, args) -> {
            System.out.println("[Proxy BEFORE] " + method.getName());
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            } finally {
                System.out.println("[Proxy AFTER] " + method.getName());
            }
        };

        return (MessageService) Proxy.newProxyInstance(
                MessageService.class.getClassLoader(),
                new Class<?>[] { MessageService.class },
                handler);
    }
}
