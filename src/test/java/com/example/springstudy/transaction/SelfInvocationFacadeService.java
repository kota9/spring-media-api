package com.example.springstudy.transaction;

import org.springframework.stereotype.Service;

@Service
public class SelfInvocationFacadeService {

    private final SelfInvocationTransactionalService transactionalService;

    public SelfInvocationFacadeService(SelfInvocationTransactionalService transactionalService) {
        this.transactionalService = transactionalService;
    }

    public void execute(String testId) {
        transactionalService.saveAndFail(testId);
    }
}
