package com.example.springstudy.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class PropagationOuterService {

    private final JdbcTemplate jdbcTemplate;
    private final PropagationInnerService innerService;

    PropagationOuterService(JdbcTemplate jdbcTemplate, PropagationInnerService innerService) {
        this.jdbcTemplate = jdbcTemplate;
        this.innerService = innerService;
    }

    @Transactional
    public void executeRequiredThenFail(String operationId) {
        saveOuter(operationId, "OUTER_REQUIRED");
        innerService.saveRequired(operationId);
        throw new IllegalStateException("Forced failure in outer REQUIRED transaction");
    }

    @Transactional
    public void executeRequiresNewThenFail(String operationId) {
        saveOuter(operationId, "OUTER_REQUIRES_NEW");
        innerService.saveRequiresNew(operationId);
        throw new IllegalStateException("Forced failure in outer REQUIRES_NEW transaction");
    }

    @Transactional
    public void executeRequiredAndCatchInnerFailure(String operationId) {
        saveOuter(operationId, "OUTER_REQUIRED_CAUGHT_FAILURE");
        try {
            innerService.saveRequiredThenFail(operationId);
        } catch (IllegalStateException ignored) {
            // The participating REQUIRED transaction has already marked the outer transaction rollback-only.
        }
    }

    private void saveOuter(String operationId, String recordType) {
        jdbcTemplate.update(
                "INSERT INTO transaction_propagation_records (operation_id, record_type, name) VALUES (?, ?, ?)",
                operationId,
                recordType,
                "outer record");
    }
}
