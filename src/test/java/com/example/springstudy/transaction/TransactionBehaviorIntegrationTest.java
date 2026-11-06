package com.example.springstudy.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
@Import({TransactionLabTestConfig.class, TransactionBoundaryTestConfig.class})
class TransactionBehaviorIntegrationTest {

    @Autowired
    private TransactionPracticeService service;

    @Autowired
    private TransactionBoundaryService boundaryService;

    @Autowired
    private TransactionOrderRepository orderRepository;

    @Autowired
    private TransactionOrderItemRepository orderItemRepository;

    @Autowired
    private TransactionHistoryRepository historyRepository;

    @Autowired
    private DataSource transactionLabDataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    @AfterEach
    void clearPracticeRecords() {
        service.clearRecords();
    }

    @Test
    void commitsRowsWhenServiceTransactionReturnsNormally() {
        service.createSuccessfully();

        assertEquals(1, service.countRecords());
    }

    @Test
    void rollsBackRowsWhenRuntimeExceptionEscapesServiceProxy() {
        assertThrows(IllegalStateException.class, service::createWithRuntimeException);

        assertEquals(0, service.countRecords());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    void checkedExceptionRollsBackOnlyWhenRollbackForIncludesItsType() {
        assertThrows(IOException.class, service::createWithCheckedException);
        assertEquals(0, service.countRecords());

        assertThrows(IOException.class, service::createWithCheckedExceptionByDefault);
        assertEquals(1, service.countRecords());
    }

    @Test
    void readOnlyFlagIsVisibleInsideProxiedServiceMethod() {
        assertTrue(service.checkReadOnlyTransaction());
    }

    @Test
    void serviceBeanIsProxiedAndSelfInvocationDoesNotStartTransaction() {
        assertTrue(AopUtils.isAopProxy(service));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        assertTrue(service.checkTransactionActive());
        assertFalse(service.invokeTransactionalMethodFromSelf());

        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    void commitsAllOrderRowsTogetherAndRollsAllBackOnFailure() {
        String committedOrderId = UUID.randomUUID().toString();
        String rolledBackOrderId = UUID.randomUUID().toString();
        assertCounts(committedOrderId, 0, 0, 0);
        assertCounts(rolledBackOrderId, 0, 0, 0);
        assertTrue(AopUtils.isAopProxy(boundaryService));
        assertTrue(transactionManager instanceof DataSourceTransactionManager);
        DataSource managerDataSource = ((DataSourceTransactionManager) transactionManager).getDataSource();
        assertSame(transactionLabDataSource, managerDataSource);
        assertSame(transactionLabDataSource, jdbcTemplate.getDataSource());

        boundaryService.createOrderSuccessfully(committedOrderId);
        assertCounts(committedOrderId, 1, 1, 1);
        assertTrue(boundaryService.wasTransactionActiveDuringOrder());

        assertThrows(RuntimeException.class, () -> boundaryService.createOrderWithFailure(rolledBackOrderId));
        assertCounts(rolledBackOrderId, 0, 0, 0);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    private void assertCounts(String orderId, int orders, int items, int history) {
        assertEquals(orders, orderRepository.countByOrderId(orderId), "order row count");
        assertEquals(items, orderItemRepository.countByOrderId(orderId), "order item row count");
        assertEquals(history, historyRepository.countByOrderId(orderId), "history row count");
    }
}
