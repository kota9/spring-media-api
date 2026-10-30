package com.example.springstudy.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PropagationInnerService {

    private final JdbcTemplate jdbcTemplate;

    PropagationInnerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveRequired(String operationId) {
        save(operationId, "INNER_REQUIRED", "required inner record");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRequiresNew(String operationId) {
        save(operationId, "INNER_REQUIRES_NEW", "requires-new inner record");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveRequiredThenFail(String operationId) {
        save(operationId, "INNER_REQUIRED_FAILURE", "required inner record before failure");
        throw new IllegalStateException("Forced failure inside REQUIRED transaction");
    }

    private void save(String operationId, String recordType, String name) {
        jdbcTemplate.update(
                "INSERT INTO transaction_propagation_records (operation_id, record_type, name) VALUES (?, ?, ?)",
                operationId,
                recordType,
                name);
    }
}
