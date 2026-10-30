package com.example.springstudy.transaction;

import java.io.IOException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionPracticeService {

    private final JdbcTemplate jdbcTemplate;

    TransactionPracticeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void createSuccessfully() {
        insert("committed");
    }

    @Transactional
    public void createWithRuntimeException() {
        insert("runtime-rollback");
        throw new IllegalStateException("Forced runtime exception");
    }

    @Transactional(rollbackFor = IOException.class)
    public void createWithCheckedException() throws IOException {
        insert("checked-rollback");
        throw new IOException("Forced checked exception");
    }

    @Transactional(readOnly = true)
    public boolean checkReadOnlyTransaction() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    @Transactional
    public boolean checkTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    public boolean invokeTransactionalMethodFromSelf() {
        return this.checkTransactionActive();
    }

    public int countRecords() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transaction_lab", Integer.class);
    }

    public void clearRecords() {
        jdbcTemplate.update("DELETE FROM transaction_lab");
    }

    private void insert(String message) {
        jdbcTemplate.update("INSERT INTO transaction_lab (message) VALUES (?)", message);
    }
}
