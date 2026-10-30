package com.example.springstudy.transaction;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TransactionLabTestConfig.class)
class ReadOnlyTransactionTest {

    @Autowired
    private TransactionPracticeService service;

    @Test
    void readOnlyTransactionFlagIsSet() {
        assertTrue(service.checkReadOnlyTransaction());
    }
}
