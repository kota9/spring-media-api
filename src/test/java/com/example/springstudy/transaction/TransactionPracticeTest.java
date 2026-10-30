package com.example.springstudy.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TransactionLabTestConfig.class)
class TransactionPracticeTest {

    @Autowired
    private TransactionPracticeService service;

    @BeforeEach
    void clearBeforeTest() {
        service.clearRecords();
    }

    @AfterEach
    void clearAfterTest() {
        service.clearRecords();
    }

    @Test
    void commitsDataAfterSuccessfulCall() {
        service.createSuccessfully();

        assertEquals(1, service.countRecords());
    }

    @Test
    void rollsBackDataAfterRuntimeException() {
        assertThrows(IllegalStateException.class, service::createWithRuntimeException);

        assertEquals(0, service.countRecords());
    }

    @Test
    void rollsBackCheckedExceptionWhenConfigured() {
        assertThrows(IOException.class, service::createWithCheckedException);

        assertEquals(0, service.countRecords());
    }
}
