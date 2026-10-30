package com.example.springstudy.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;

@SpringBootTest
@Import({TransactionLabTestConfig.class, TransactionIsolationTestConfig.class})
class TransactionIsolationTest {

    private static final long TEST_TIMEOUT_SECONDS = 15;
    private static final int INITIAL_AMOUNT = 100;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IsolationReaderService readerService;

    @Autowired
    private IsolationWriterService writerService;

    @BeforeEach
    void clearRecords() {
        jdbcTemplate.update("DELETE FROM transaction_isolation_records");
    }

    @AfterEach
    void cleanRecordsAfterTest() {
        jdbcTemplate.update("DELETE FROM transaction_isolation_records");
    }

    @Test
    void observesWhetherReadUncommittedSeesUncommittedUpdate(TestReporter reporter) throws Exception {
        String recordKey = UUID.randomUUID().toString();
        insertInitialRecord(recordKey, "dirty-read-category");
        CountDownLatch valueChanged = new CountDownLatch(1);
        CountDownLatch readerFinished = new CountDownLatch(1);

        withExecutor(executor -> {
            Future<IsolationReaderService.ReadObservation<Integer>> reader = executor.submit(
                    () -> readerService.readUncommittedAmount(recordKey, valueChanged, readerFinished));
            Future<Connection> writer = executor.submit(
                    () -> writerService.updateAmountAndHold(recordKey, 200, valueChanged, readerFinished));

            IsolationReaderService.ReadObservation<Integer> read = get(reader);
            Connection writerConnection = get(writer);

            assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, read.isolationLevel());
            assertNotSame(read.connection(), writerConnection, "Reader and writer must use separate connections");
            assertTrue(read.value() == INITIAL_AMOUNT || read.value() == 200,
                    "Reader should see either the committed value or the uncommitted update");
            assertEquals(200, jdbcTemplate.queryForObject(
                    "SELECT amount FROM transaction_isolation_records WHERE record_key = ?", Integer.class, recordKey));

            boolean dirtyRead = read.value() == 200;
            reporter.publishEntry("Dirty Read", dirtyRead
                    ? "Observed the uncommitted value 200"
                    : "Did not observe the uncommitted value; observed committed value 100");
            return null;
        });
    }

    @Test
    void readCommittedAllowsNonRepeatableRead(TestReporter reporter) throws Exception {
        String recordKey = UUID.randomUUID().toString();
        insertInitialRecord(recordKey, "non-repeatable-read-category");

        IsolationReaderService.BeforeAfter<Integer> result = readTwiceWithWriter(
                (firstRead, committed) -> readerService.readAmountTwiceReadCommitted(recordKey, firstRead, committed),
                () -> writerService.updateAmount(recordKey, 200),
                Connection.TRANSACTION_READ_COMMITTED);

        assertEquals(new IsolationReaderService.BeforeAfter<>(100, 200), result);
        reporter.publishEntry("Non-Repeatable Read / READ_COMMITTED", result.toString());
    }

    @Test
    void repeatableReadPreventsNonRepeatableRead(TestReporter reporter) throws Exception {
        String recordKey = UUID.randomUUID().toString();
        insertInitialRecord(recordKey, "repeatable-read-category");

        IsolationReaderService.BeforeAfter<Integer> result = readTwiceWithWriter(
                (firstRead, committed) -> readerService.readAmountTwiceRepeatableRead(recordKey, firstRead, committed),
                () -> writerService.updateAmount(recordKey, 200),
                Connection.TRANSACTION_REPEATABLE_READ);

        assertEquals(new IsolationReaderService.BeforeAfter<>(100, 100), result);
        reporter.publishEntry("Non-Repeatable Read / REPEATABLE_READ", result.toString());
    }

    @Test
    void readCommittedAllowsPhantomRead(TestReporter reporter) throws Exception {
        String category = "phantom-rc-" + UUID.randomUUID();
        insertInitialRecord(UUID.randomUUID().toString(), category);

        IsolationReaderService.BeforeAfter<Integer> result = countTwiceWithInsert(
                (firstRead, committed) -> readerService.countCategoryTwiceReadCommitted(category, firstRead, committed),
                category,
                Connection.TRANSACTION_READ_COMMITTED);

        assertEquals(new IsolationReaderService.BeforeAfter<>(1, 2), result);
        reporter.publishEntry("Phantom Read / READ_COMMITTED", result.toString());
    }

    @Test
    void repeatableReadPhantomBehaviorIsObservedAndReported(TestReporter reporter) throws Exception {
        String category = "phantom-rr-" + UUID.randomUUID();
        insertInitialRecord(UUID.randomUUID().toString(), category);

        IsolationReaderService.BeforeAfter<Integer> result = countTwiceWithInsert(
                (firstRead, committed) -> readerService.countCategoryTwiceRepeatableRead(category, firstRead, committed),
                category,
                Connection.TRANSACTION_REPEATABLE_READ);

        assertTrue(result.before() == 1 && (result.after() == 1 || result.after() == 2),
                "H2 may retain the original predicate result or expose the committed inserted row");
        reporter.publishEntry("Phantom Read / REPEATABLE_READ", result.toString());
    }

    @Test
    void serializableIsolationIsAppliedBySpringTransactionProxy() {
        String recordKey = UUID.randomUUID().toString();
        insertInitialRecord(recordKey, "serializable-category");

        IsolationReaderService.ReadObservation<Integer> observation = readerService.readAtSerializable(recordKey);

        assertEquals(Connection.TRANSACTION_SERIALIZABLE, observation.isolationLevel());
        assertEquals(INITIAL_AMOUNT, observation.value());
    }

    private IsolationReaderService.BeforeAfter<Integer> readTwiceWithWriter(
            ReaderCall<IsolationReaderService.BeforeAfter<Integer>> readerCall,
            WriterCall writerCall,
            int expectedIsolation) throws Exception {
        CountDownLatch firstReadDone = new CountDownLatch(1);
        CountDownLatch writerCommitted = new CountDownLatch(1);

        return withExecutor(executor -> {
            Future<IsolationReaderService.ReadObservation<IsolationReaderService.BeforeAfter<Integer>>> reader =
                    executor.submit(() -> readerCall.read(firstReadDone, writerCommitted));
            Future<Connection> writer = executor.submit(() -> {
                await(firstReadDone, "reader's first query");
                try {
                    return writerCall.write();
                } finally {
                    writerCommitted.countDown();
                }
            });

            IsolationReaderService.ReadObservation<IsolationReaderService.BeforeAfter<Integer>> observation = get(reader);
            Connection writerConnection = get(writer);
            assertNotSame(observation.connection(), writerConnection, "Reader and writer must use separate connections");
            assertEquals(expectedIsolation, observation.isolationLevel());
            return observation.value();
        });
    }

    private IsolationReaderService.BeforeAfter<Integer> countTwiceWithInsert(
            ReaderCall<IsolationReaderService.BeforeAfter<Integer>> readerCall,
            String category,
            int expectedIsolation) throws Exception {
        return readTwiceWithWriter(readerCall,
                () -> writerService.insertRecord(UUID.randomUUID().toString(), "new category row", 10, category),
                expectedIsolation);
    }

    private void insertInitialRecord(String recordKey, String category) {
        jdbcTemplate.update(
                "INSERT INTO transaction_isolation_records (record_key, name, amount, category) VALUES (?, ?, ?, ?)",
                recordKey,
                "initial record",
                INITIAL_AMOUNT,
                category);
    }

    private static <T> T get(Future<T> future) throws Exception {
        return future.get(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static void await(CountDownLatch latch, String description) {
        try {
            if (!latch.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for " + description);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for " + description, exception);
        }
    }

    private static <T> T withExecutor(ExecutorWork<T> work) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            return work.run(executor);
        } finally {
            executor.shutdownNow();
            if (!executor.awaitTermination(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Executor did not terminate within the timeout");
            }
        }
    }

    @FunctionalInterface
    private interface ReaderCall<T> {
        IsolationReaderService.ReadObservation<T> read(CountDownLatch firstReadDone, CountDownLatch writerCommitted);
    }

    @FunctionalInterface
    private interface WriterCall {
        Connection write();
    }

    @FunctionalInterface
    private interface ExecutorWork<T> {
        T run(ExecutorService executor) throws Exception;
    }
}
