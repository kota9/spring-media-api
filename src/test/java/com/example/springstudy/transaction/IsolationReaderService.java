package com.example.springstudy.transaction;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class IsolationReaderService {

    private static final long WAIT_SECONDS = 10;

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    IsolationReaderService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public ReadObservation<Integer> readUncommittedAmount(
            String recordKey,
            CountDownLatch valueChanged,
            CountDownLatch readerFinished) {
        try {
            await(valueChanged, "writer changed the amount");
            return observation(readAmount(recordKey));
        } finally {
            readerFinished.countDown();
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReadObservation<BeforeAfter<Integer>> readAmountTwiceReadCommitted(
            String recordKey,
            CountDownLatch firstReadDone,
            CountDownLatch writerCommitted) {
        int before = readAmount(recordKey);
        firstReadDone.countDown();
        await(writerCommitted, "writer commit");
        return observation(new BeforeAfter<>(before, readAmount(recordKey)));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReadObservation<BeforeAfter<Integer>> readAmountTwiceRepeatableRead(
            String recordKey,
            CountDownLatch firstReadDone,
            CountDownLatch writerCommitted) {
        int before = readAmount(recordKey);
        firstReadDone.countDown();
        await(writerCommitted, "writer commit");
        return observation(new BeforeAfter<>(before, readAmount(recordKey)));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReadObservation<BeforeAfter<Integer>> countCategoryTwiceReadCommitted(
            String category,
            CountDownLatch firstReadDone,
            CountDownLatch writerCommitted) {
        int before = countCategory(category);
        firstReadDone.countDown();
        await(writerCommitted, "writer commit");
        return observation(new BeforeAfter<>(before, countCategory(category)));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReadObservation<BeforeAfter<Integer>> countCategoryTwiceRepeatableRead(
            String category,
            CountDownLatch firstReadDone,
            CountDownLatch writerCommitted) {
        int before = countCategory(category);
        firstReadDone.countDown();
        await(writerCommitted, "writer commit");
        return observation(new BeforeAfter<>(before, countCategory(category)));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ReadObservation<Integer> readAtSerializable(String recordKey) {
        return observation(readAmount(recordKey));
    }

    private int readAmount(String recordKey) {
        return jdbcTemplate.queryForObject(
                "SELECT amount FROM transaction_isolation_records WHERE record_key = ?",
                Integer.class,
                recordKey);
    }

    private int countCategory(String category) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transaction_isolation_records WHERE category = ?",
                Integer.class,
                category);
    }

    private <T> ReadObservation<T> observation(T value) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
        return new ReadObservation<>(value, connection, isolationLevel);
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

    public record BeforeAfter<T>(T before, T after) {
    }

    public record ReadObservation<T>(T value, Connection connection, Integer isolationLevel) {
    }
}
