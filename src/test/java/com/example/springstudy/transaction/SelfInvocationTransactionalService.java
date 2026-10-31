package com.example.springstudy.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class SelfInvocationTransactionalService {

    private final JdbcTemplate jdbcTemplate;
    private volatile boolean transactionActiveAtSave;

    public SelfInvocationTransactionalService(JdbcTemplate transactionLabJdbcTemplate) {
        this.jdbcTemplate = transactionLabJdbcTemplate;
    }

    @Transactional
    public void saveAndFail(String testId) {
        transactionActiveAtSave = TransactionSynchronizationManager.isActualTransactionActive();
        jdbcTemplate.update(
                "INSERT INTO transaction_self_invocation_records (test_id, message) VALUES (?, ?)",
                testId,
                "saved before forced transactional failure");
        throw new IllegalStateException("Forced transactional failure after insert: " + testId);
    }

    public boolean wasTransactionActiveAtSave() {
        return transactionActiveAtSave;
    }
}
