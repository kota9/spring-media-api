package com.example.springstudy.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class SelfInvocationBadService {

    private final JdbcTemplate jdbcTemplate;
    private volatile boolean transactionActiveAtFailure;

    public SelfInvocationBadService(JdbcTemplate transactionLabJdbcTemplate) {
        this.jdbcTemplate = transactionLabJdbcTemplate;
    }

    public void execute(String testId) {
        saveAndFail(testId);
    }

    @Transactional
    public void saveAndFail(String testId) {
        transactionActiveAtFailure = TransactionSynchronizationManager.isActualTransactionActive();
        jdbcTemplate.update(
                "INSERT INTO transaction_self_invocation_records (test_id, message) VALUES (?, ?)",
                testId,
                "saved before forced failure");
        throw new IllegalStateException("Forced failure after insert: " + testId);
    }

    public boolean wasTransactionActiveAtFailure() {
        return transactionActiveAtFailure;
    }
}
