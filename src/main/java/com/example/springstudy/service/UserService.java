package com.example.springstudy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springstudy.domain.User;
import com.example.springstudy.dto.UserRequest;
import com.example.springstudy.exception.UserNotFoundException;
import com.example.springstudy.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User create(UserRequest request) {
        return userRepository.save(request.name(), request.email());
    }

    public User update(Long userId, UserRequest request) {
        findById(userId);
        return userRepository.update(userId, request.name(), request.email());
    }

    public void delete(Long userId) {
        findById(userId);
        userRepository.deleteById(userId);
    }
}
