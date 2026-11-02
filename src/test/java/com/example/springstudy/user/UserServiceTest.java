package com.example.springstudy.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.springstudy.domain.User;
import com.example.springstudy.dto.UserRequest;
import com.example.springstudy.exception.DuplicateUserEmailException;
import com.example.springstudy.exception.UserNotFoundException;
import com.example.springstudy.repository.CommentRepository;
import com.example.springstudy.repository.UserRepository;
import com.example.springstudy.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원을 등록하고 생성된 회원 정보를 반환한다")
    void createUser() {
        // Given
        UserRequest request = new UserRequest("Kim", "kim@example.com");
        User savedUser = new User(1L, request.name(), request.email());
        when(userRepository.save(request.name(), request.email())).thenReturn(savedUser);

        // When
        User result = userService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(savedUser, result);
        verify(userRepository).save(request.name(), request.email());
    }

    @Test
    @DisplayName("존재하는 ID로 회원을 조회하면 회원 정보를 반환한다")
    void findUserById() {
        // Given
        Long userId = 1L;
        User expected = new User(userId, "Kim", "kim@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expected));

        // When
        User result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 회원을 조회하면 UserNotFoundException을 던진다")
    void throwsWhenUserDoesNotExist() {
        // Given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("중복 이메일이면 저장소의 DuplicateUserEmailException을 전달한다")
    void throwsWhenEmailIsDuplicated() {
        // Given
        UserRequest request = new UserRequest("Kim", "duplicate@example.com");
        when(userRepository.save(request.name(), request.email()))
                .thenThrow(new DuplicateUserEmailException(request.email()));

        // When / Then
        assertThrows(DuplicateUserEmailException.class, () -> userService.create(request));
        verify(userRepository).save(request.name(), request.email());
    }

    @Test
    @DisplayName("회원을 수정하고 수정된 회원 정보를 반환한다")
    void updateUser() {
        // Given
        Long userId = 1L;
        UserRequest request = new UserRequest("New name", "new@example.com");
        User existing = new User(userId, "Old name", "old@example.com");
        User updated = new User(userId, request.name(), request.email());
        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.update(userId, request.name(), request.email())).thenReturn(updated);

        // When
        User result = userService.update(userId, request);

        // Then
        assertNotNull(result);
        assertEquals(updated, result);
        verify(userRepository).findById(userId);
        verify(userRepository).update(userId, request.name(), request.email());
    }

    @Test
    @DisplayName("존재하지 않는 회원은 수정하지 않고 UserNotFoundException을 던진다")
    void throwsWhenUpdatingMissingUser() {
        // Given
        Long userId = 99L;
        UserRequest request = new UserRequest("New name", "new@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> userService.update(userId, request));
        verify(userRepository).findById(userId);
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("회원을 삭제하면 해당 회원의 댓글과 회원 정보를 삭제한다")
    void deleteUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User(userId, "Kim", "kim@example.com")));

        // When
        userService.delete(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(commentRepository).deleteByUserId(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 회원은 삭제하지 않고 UserNotFoundException을 던진다")
    void throwsWhenDeletingMissingUser() {
        // Given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> userService.delete(userId));
        verify(userRepository).findById(userId);
        verifyNoInteractions(commentRepository);
    }
}
