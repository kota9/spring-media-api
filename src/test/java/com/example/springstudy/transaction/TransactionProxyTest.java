package com.example.springstudy.transaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
@Import(TransactionLabTestConfig.class)
class TransactionProxyTest {

    @Autowired
    private TransactionPracticeService service;

    @Test
    void serviceIsSpringProxy() {
        assertTrue(AopUtils.isAopProxy(service));
    }

    @Test
    void externalProxyCallStartsTransactionButSelfInvocationDoesNot() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertTrue(service.checkTransactionActive());
        assertFalse(service.invokeTransactionalMethodFromSelf());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }
}
