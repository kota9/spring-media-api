package com.example.springstudy.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
@Import({TransactionLabTestConfig.class, SelfInvocationBadTestConfig.class})
class SelfInvocationBadTest {

    @Autowired
    private SelfInvocationBadService springService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<String> testIds = new ArrayList<>();

    @AfterEach
    void removeTestRows() {
        for (String testId : testIds) {
            jdbcTemplate.update(
                    "DELETE FROM transaction_self_invocation_records WHERE test_id = ?",
                    testId);
        }
    }

    @Test
    void springBeanSelfInvocationBypassesTransactionProxyAndLeavesInsertedRow() {
        String testId = newTestId();
        assertTrue(AopUtils.isAopProxy(springService));
        assertEquals(0, findRows(testId));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> springService.execute(testId));

        assertTrue(exception.getMessage().contains(testId));
        assertFalse(springService.wasTransactionActiveAtFailure());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertEquals(1, findRows(testId));
    }

    @Test
    void manuallyCreatedObjectHasNoSpringProxyAndAlsoLeavesInsertedRow() {
        String testId = newTestId();
        SelfInvocationBadService manuallyCreatedService = new SelfInvocationBadService(jdbcTemplate);

        assertFalse(AopUtils.isAopProxy(manuallyCreatedService));
        assertEquals(0, findRows(testId));

        assertThrows(IllegalStateException.class, () -> manuallyCreatedService.execute(testId));

        assertFalse(manuallyCreatedService.wasTransactionActiveAtFailure());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertEquals(1, findRows(testId));
    }

    private String newTestId() {
        String testId = UUID.randomUUID().toString();
        testIds.add(testId);
        return testId;
    }

    private int findRows(String testId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transaction_self_invocation_records WHERE test_id = ?",
                Integer.class,
                testId);
    }
}
