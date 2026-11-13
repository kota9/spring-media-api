package com.example.springstudy.repository;

import com.example.springstudy.domain.User;
import com.example.springstudy.entity.UserRole;
import com.example.springstudy.exception.DuplicateUserEmailException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class UserRepository {
    private final JpaUserRepository users;

    public UserRepository(JpaUserRepository users) {
        this.users = users;
    }

    public List<User> findAll() {
        return users.findAll().stream().map(UserRepository::toDomain).toList();
    }

    public Optional<User> findById(Long userId) {
        return users.findById(userId).map(UserRepository::toDomain);
    }

    @Transactional
    public User save(String name, String email, String passwordHash) {
        ensureEmailAvailable(email, null);
        return toDomain(users.save(new com.example.springstudy.entity.User(email, name, passwordHash, UserRole.USER)));
    }

    @Transactional
    public User update(Long userId, String name, String email) {
        ensureEmailAvailable(email, userId);
        com.example.springstudy.entity.User user = users.findById(userId).orElseThrow();
        user.changeName(name);
        user.changeEmail(email);
        return toDomain(user);
    }

    @Transactional
    public void deleteById(Long userId) {
        users.deleteById(userId);
    }

    private void ensureEmailAvailable(String email, Long excludedUserId) {
        users.findByEmail(email).filter(existing -> !existing.getId().equals(excludedUserId))
                .ifPresent(existing -> { throw new DuplicateUserEmailException(email); });
    }

    private static User toDomain(com.example.springstudy.entity.User user) {
        return new User(user.getId(), user.getName(), user.getEmail());
    }
}
