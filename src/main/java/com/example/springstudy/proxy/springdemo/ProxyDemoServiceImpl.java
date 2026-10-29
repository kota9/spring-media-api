package com.example.springstudy.proxy.springdemo;

import org.springframework.stereotype.Service;

@Service
public class ProxyDemoServiceImpl implements ProxyDemoService {

    @Override
    public String execute(String message) {
        return "Executed: " + message;
    }
}
