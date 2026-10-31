package com.example.springstudy.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TransactionBoundaryService {

    private final TransactionOrderRepository orderRepository;
    private final TransactionOrderItemRepository orderItemRepository;
    private final TransactionHistoryRepository historyRepository;
    private volatile boolean transactionActiveDuringOrder;

    public TransactionBoundaryService(
            TransactionOrderRepository orderRepository,
            TransactionOrderItemRepository orderItemRepository,
            TransactionHistoryRepository historyRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void createOrderSuccessfully(String orderId) {
        transactionActiveDuringOrder = TransactionSynchronizationManager.isActualTransactionActive();
        orderRepository.save(orderId);
        orderItemRepository.save(orderId);
        historyRepository.saveOrderCreated(orderId);
    }

    @Transactional
    public void createOrderWithFailure(String orderId) {
        transactionActiveDuringOrder = TransactionSynchronizationManager.isActualTransactionActive();
        orderRepository.save(orderId);
        orderItemRepository.save(orderId);
        historyRepository.saveOrderCreated(orderId);
        throw new RuntimeException("주문 처리 실패");
    }

    public boolean wasTransactionActiveDuringOrder() {
        return transactionActiveDuringOrder;
    }
}
