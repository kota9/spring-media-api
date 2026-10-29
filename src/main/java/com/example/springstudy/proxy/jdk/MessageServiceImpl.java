package com.example.springstudy.proxy.jdk;

public class MessageServiceImpl implements MessageService {

    @Override
    public String send(String message) {
        return "Processed message: " + message;
    }
}
