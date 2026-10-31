package com.example.springstudy.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionOrderRepository(JdbcTemplate transactionLabJdbcTemplate) {
        this.jdbcTemplate = transactionLabJdbcTemplate;
    }

    public void save(String orderId) {
        jdbcTemplate.update(
                "INSERT INTO transaction_boundary_orders (order_id, customer_name) VALUES (?, ?)",
                orderId,
                "Transaction Boundary Customer");
    }

    public int countByOrderId(String orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transaction_boundary_orders WHERE order_id = ?",
                Integer.class,
                orderId);
    }

    public void deleteByOrderId(String orderId) {
        jdbcTemplate.update("DELETE FROM transaction_boundary_orders WHERE order_id = ?", orderId);
    }
}
