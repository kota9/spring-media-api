package com.example.springstudy.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.UnexpectedRollbackException;

@SpringBootTest
@Import({TransactionLabTestConfig.class, TransactionPropagationTestConfig.class})
class TransactionPropagationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PropagationOuterService outerService;

    @Autowired
    private PropagationInnerService innerService;

    @Test
    void requiredInnerWorkRollsBackWithOuterFailure() {
        String operationId = UUID.randomUUID().toString();
        assertTrue(AopUtils.isAopProxy(outerService));
        assertTrue(AopUtils.isAopProxy(innerService));

        assertThrows(IllegalStateException.class, () -> outerService.executeRequiredThenFail(operationId));

        assertEquals(List.of(), findRecordTypes(operationId));
    }

    @Test
    void requiresNewInnerWorkCommitsEvenWhenOuterTransactionRollsBack() {
        String operationId = UUID.randomUUID().toString();

        assertThrows(IllegalStateException.class, () -> outerService.executeRequiresNewThenFail(operationId));

        assertEquals(List.of("INNER_REQUIRES_NEW"), findRecordTypes(operationId));
        assertEquals(0, countRecordType(operationId, "OUTER_REQUIRES_NEW"));
        assertEquals(1, countRecordType(operationId, "INNER_REQUIRES_NEW"));
    }

    @Test
    void caughtRequiredInnerFailureStillCausesUnexpectedRollbackAtOuterBoundary() {
        String operationId = UUID.randomUUID().toString();

        assertThrows(
                UnexpectedRollbackException.class,
                () -> outerService.executeRequiredAndCatchInnerFailure(operationId));

        assertEquals(List.of(), findRecordTypes(operationId));
    }

    private List<String> findRecordTypes(String operationId) {
        return jdbcTemplate.queryForList(
                "SELECT record_type FROM transaction_propagation_records WHERE operation_id = ? ORDER BY id",
                String.class,
                operationId);
    }

    private int countRecordType(String operationId, String recordType) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transaction_propagation_records WHERE operation_id = ? AND record_type = ?",
                Integer.class,
                operationId,
                recordType);
    }
}
