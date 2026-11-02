package com.example.springstudy.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.springstudy.domain.Comment;
import com.example.springstudy.dto.CommentCreateRequest;
import com.example.springstudy.dto.CommentUpdateRequest;
import com.example.springstudy.exception.CommentNotFoundException;
import com.example.springstudy.exception.ContentNotFoundException;
import com.example.springstudy.exception.UserNotFoundException;
import com.example.springstudy.repository.CommentRepository;
import com.example.springstudy.service.CommentService;
import com.example.springstudy.service.ContentService;
import com.example.springstudy.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ContentService contentService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("댓글을 등록하고 저장된 댓글을 반환한다")
    void createComment() {
        // Given
        Long contentId = 10L;
        Long userId = 20L;
        CommentCreateRequest request = new CommentCreateRequest(userId, "Great post!");
        Comment saved = new Comment(1L, contentId, userId, request.body());
        when(commentRepository.save(contentId, userId, request.body())).thenReturn(saved);

        // When
        Comment result = commentService.create(contentId, request);

        // Then
        assertNotNull(result);
        assertEquals(saved, result);
        verify(contentService).findById(contentId);
        verify(userService).findById(userId);
        verify(commentRepository).save(contentId, userId, request.body());
    }

    @Test
    @DisplayName("존재하는 댓글 ID로 조회하면 댓글을 반환한다")
    void findCommentById() {
        // Given
        Long commentId = 1L;
        Comment expected = new Comment(commentId, 10L, 20L, "Great post!");
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(expected));

        // When
        Comment result = commentService.findById(commentId);

        // Then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 조회하면 CommentNotFoundException을 던진다")
    void throwsWhenCommentDoesNotExist() {
        // Given
        Long commentId = 99L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(CommentNotFoundException.class, () -> commentService.findById(commentId));
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("콘텐츠가 없으면 댓글을 저장하지 않고 예외를 던진다")
    void doesNotCreateCommentWhenContentDoesNotExist() {
        // Given
        Long contentId = 99L;
        CommentCreateRequest request = new CommentCreateRequest(20L, "Great post!");
        when(contentService.findById(contentId)).thenThrow(new ContentNotFoundException(contentId));

        // When / Then
        assertThrows(ContentNotFoundException.class, () -> commentService.create(contentId, request));
        verify(contentService).findById(contentId);
        verifyNoInteractions(commentRepository, userService);
    }

    @Test
    @DisplayName("작성자가 없으면 댓글을 저장하지 않고 예외를 전달한다")
    void doesNotCreateCommentWhenUserDoesNotExist() {
        // Given
        Long contentId = 10L;
        Long userId = 99L;
        CommentCreateRequest request = new CommentCreateRequest(userId, "Great post!");
        when(userService.findById(userId)).thenThrow(new UserNotFoundException(userId));

        // When / Then
        assertThrows(UserNotFoundException.class, () -> commentService.create(contentId, request));
        verify(contentService).findById(contentId);
        verify(userService).findById(userId);
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("댓글을 수정하고 수정된 댓글을 반환한다")
    void updateComment() {
        // Given
        Long commentId = 1L;
        Comment existing = new Comment(commentId, 10L, 20L, "Old comment");
        CommentUpdateRequest request = new CommentUpdateRequest("Updated comment");
        Comment updated = new Comment(commentId, 10L, 20L, request.body());
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));
        when(commentRepository.update(commentId, request.body())).thenReturn(updated);

        // When
        Comment result = commentService.update(commentId, request);

        // Then
        assertNotNull(result);
        assertEquals(updated, result);
        verify(commentRepository).findById(commentId);
        verify(commentRepository).update(commentId, request.body());
    }

    @Test
    @DisplayName("존재하지 않는 댓글은 수정하지 않고 예외를 던진다")
    void throwsWhenUpdatingMissingComment() {
        // Given
        Long commentId = 99L;
        CommentUpdateRequest request = new CommentUpdateRequest("Updated comment");
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(CommentNotFoundException.class, () -> commentService.update(commentId, request));
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).update(commentId, request.body());
    }

    @Test
    @DisplayName("댓글을 삭제하면 저장소에 삭제 요청을 전달한다")
    void deleteComment() {
        // Given
        Long commentId = 1L;
        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(new Comment(commentId, 10L, 20L, "Comment")));

        // When
        commentService.delete(commentId);

        // Then
        verify(commentRepository).findById(commentId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("존재하지 않는 댓글은 삭제하지 않고 예외를 던진다")
    void throwsWhenDeletingMissingComment() {
        // Given
        Long commentId = 99L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(CommentNotFoundException.class, () -> commentService.delete(commentId));
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).deleteById(commentId);
    }
}
