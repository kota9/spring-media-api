package com.example.springstudy.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
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
class TransactionBoundaryTest {

    @Autowired
    private TransactionBoundaryService boundaryService;

    @Autowired
    private TransactionOrderRepository orderRepository;

    @Autowired
    private TransactionOrderItemRepository orderItemRepository;

    @Autowired
    private TransactionHistoryRepository historyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource transactionLabDataSource;

    @Autowired
    private PlatformTransactionManager transactionLabTransactionManager;

    private final List<String> orderIds = new ArrayList<>();

    @AfterEach
    void removeTestOrders() {
        for (String orderId : orderIds) {
            historyRepository.deleteByOrderId(orderId);
            orderItemRepository.deleteByOrderId(orderId);
            orderRepository.deleteByOrderId(orderId);
        }
    }

    @Test
    void serviceBoundaryCommitsAllRowsAndRollsBackAllRowsOnFailure() {
        String successfulOrderId = newOrderId();
        String failedOrderId = newOrderId();

        assertTrue(AopUtils.isAopProxy(boundaryService));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertSameDataSourceForJdbcAndTransactionManager();

        boundaryService.createOrderSuccessfully(successfulOrderId);

        assertTrue(boundaryService.wasTransactionActiveDuringOrder());
        assertCounts(successfulOrderId, 1, 1, 1);

        assertThrows(
                RuntimeException.class,
                () -> boundaryService.createOrderWithFailure(failedOrderId));

        assertTrue(boundaryService.wasTransactionActiveDuringOrder());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertCounts(failedOrderId, 0, 0, 0);
    }

    private String newOrderId() {
        String orderId = UUID.randomUUID().toString();
        orderIds.add(orderId);
        return orderId;
    }

    private void assertCounts(String orderId, int orders, int items, int history) {
        assertEquals(orders, orderRepository.countByOrderId(orderId), "order row count");
        assertEquals(items, orderItemRepository.countByOrderId(orderId), "order item row count");
        assertEquals(history, historyRepository.countByOrderId(orderId), "history row count");
    }

    private void assertSameDataSourceForJdbcAndTransactionManager() {
        assertTrue(transactionLabTransactionManager instanceof DataSourceTransactionManager);
        DataSource transactionManagerDataSource =
                ((DataSourceTransactionManager) transactionLabTransactionManager).getDataSource();
        assertTrue(transactionManagerDataSource == transactionLabDataSource);
        assertTrue(jdbcTemplate.getDataSource() == transactionLabDataSource);
        assertTrue(transactionManagerDataSource == jdbcTemplate.getDataSource());
    }
}
