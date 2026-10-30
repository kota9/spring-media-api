package com.example.springstudy.transaction;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

public class IsolationWriterService {

    private static final long WAIT_SECONDS = 10;

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    IsolationWriterService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Connection updateAmountAndHold(
            String recordKey,
            int amount,
            CountDownLatch valueChanged,
            CountDownLatch readerFinished) {
        updateAmount(recordKey, amount);
        valueChanged.countDown();
        await(readerFinished, "reader to finish reading the uncommitted value");
        return currentConnection();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Connection updateAmount(String recordKey, int amount) {
        jdbcTemplate.update(
                "UPDATE transaction_isolation_records SET amount = ? WHERE record_key = ?",
                amount,
                recordKey);
        return currentConnection();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Connection insertRecord(String recordKey, String name, int amount, String category) {
        jdbcTemplate.update(
                "INSERT INTO transaction_isolation_records (record_key, name, amount, category) VALUES (?, ?, ?, ?)",
                recordKey,
                name,
                amount,
                category);
        return currentConnection();
    }

    private Connection currentConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    private static void await(CountDownLatch latch, String description) {
        try {
            if (!latch.await(WAIT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for " + description);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for " + description, exception);
        }
    }
}
