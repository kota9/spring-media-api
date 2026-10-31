package com.example.springstudy.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionOrderItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionOrderItemRepository(JdbcTemplate transactionLabJdbcTemplate) {
        this.jdbcTemplate = transactionLabJdbcTemplate;
    }

    public void save(String orderId) {
        jdbcTemplate.update(
                "INSERT INTO transaction_boundary_order_items (order_id, product_name, quantity) VALUES (?, ?, ?)",
                orderId,
                "Spring Study Book",
                2);
    }

    public int countByOrderId(String orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transaction_boundary_order_items WHERE order_id = ?",
                Integer.class,
                orderId);
    }

    public void deleteByOrderId(String orderId) {
        jdbcTemplate.update("DELETE FROM transaction_boundary_order_items WHERE order_id = ?", orderId);
    }
}
