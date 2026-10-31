package com.example.springstudy.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionHistoryRepository(JdbcTemplate transactionLabJdbcTemplate) {
        this.jdbcTemplate = transactionLabJdbcTemplate;
    }

    public void saveOrderCreated(String orderId) {
        jdbcTemplate.update(
                "INSERT INTO transaction_boundary_history (order_id, event_type) VALUES (?, ?)",
                orderId,
                "ORDER_CREATED");
    }

    public int countByOrderId(String orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transaction_boundary_history WHERE order_id = ?",
                Integer.class,
                orderId);
    }

    public void deleteByOrderId(String orderId) {
        jdbcTemplate.update("DELETE FROM transaction_boundary_history WHERE order_id = ?", orderId);
    }
}
