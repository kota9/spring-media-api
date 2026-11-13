package com.example.springstudy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springstudy.domain.User;
import com.example.springstudy.dto.UserRequest;
import com.example.springstudy.dto.UserCreateRequest;
import com.example.springstudy.exception.UserNotFoundException;
import com.example.springstudy.repository.CommentRepository;
import com.example.springstudy.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, CommentRepository commentRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public User create(UserCreateRequest request) {
        return userRepository.save(request.name(), request.email(), passwordHasher.hash(request.password()));
    }

    @Transactional
    public User update(Long userId, UserRequest request) {
        findById(userId);
        return userRepository.update(userId, request.name(), request.email());
    }

    @Transactional
    public void delete(Long userId) {
        findById(userId);
        commentRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
