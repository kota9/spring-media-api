package com.example.springstudy.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.example.springstudy.domain.User;
import com.example.springstudy.exception.DuplicateUserEmailException;

@Repository
public class UserRepository {
    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public List<User> findAll() {
        return users.values().stream().sorted(Comparator.comparing(User::id)).toList();
    }

    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public synchronized User save(String name, String email) {
        ensureEmailAvailable(email, null);
        User user = new User(idGenerator.incrementAndGet(), name, email);
        users.put(user.id(), user);
        return user;
    }

    public synchronized User update(Long userId, String name, String email) {
        ensureEmailAvailable(email, userId);
        User user = new User(userId, name, email);
        users.put(userId, user);
        return user;
    }

    public void deleteById(Long userId) {
        users.remove(userId);
    }

    private void ensureEmailAvailable(String email, Long excludedUserId) {
        boolean alreadyUsed = users.values().stream()
                .anyMatch(user -> !user.id().equals(excludedUserId) && user.email().equals(email));
        if (alreadyUsed) {
            throw new DuplicateUserEmailException(email);
        }
    }
}
