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
class SelfInvocationFixedTest {

    @Autowired
    private SelfInvocationBadService badService;

    @Autowired
    private SelfInvocationFacadeService facadeService;

    @Autowired
    private SelfInvocationTransactionalService transactionalService;

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
    void separatedTransactionalServiceProxyRollsBackAndContrastsWithSelfInvocation() {
        String badTestId = newTestId();
        String fixedTestId = newTestId();

        assertTrue(AopUtils.isAopProxy(badService));
        assertTrue(AopUtils.isAopProxy(transactionalService));
        assertFalse(AopUtils.isAopProxy(facadeService));
        assertEquals(0, findRows(badTestId));
        assertEquals(0, findRows(fixedTestId));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        assertThrows(IllegalStateException.class, () -> badService.execute(badTestId));

        assertFalse(badService.wasTransactionActiveAtFailure());
        assertEquals(1, findRows(badTestId), "Self invocation bypasses the transaction proxy");

        assertThrows(IllegalStateException.class, () -> facadeService.execute(fixedTestId));

        assertTrue(transactionalService.wasTransactionActiveAtSave());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertEquals(0, findRows(fixedTestId), "The proxied transactional call rolls back the insert");
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
