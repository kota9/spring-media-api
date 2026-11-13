package com.example.springstudy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.springstudy.domain.Content;
import com.example.springstudy.exception.ContentNotFoundException;
import com.example.springstudy.repository.CommentRepository;
import com.example.springstudy.repository.ContentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ContentService contentService;

    @Test
    @DisplayName("콘텐츠를 등록하면 저장소에 저장하고 생성된 콘텐츠를 반환한다")
    void createContent() {
        // Given
        String title = "Spring Test";
        String description = "Mockito unit test";
        Content savedContent = new Content(1L, title, description);
        when(contentRepository.save(title, description, 4L, 7L)).thenReturn(savedContent);

        // When
        Content result = contentService.create(title, description, 4L, 7L);

        // Then
        assertNotNull(result);
        assertEquals(savedContent, result);
        verify(contentRepository).save(title, description, 4L, 7L);
    }

    @Test
    @DisplayName("ID로 콘텐츠를 조회하면 저장소의 콘텐츠를 반환한다")
    void findContentById() {
        // Given
        Long id = 1L;
        Content expected = new Content(id, "Spring Test", "Mockito unit test");
        when(contentRepository.findById(id)).thenReturn(Optional.of(expected));

        // When
        Content result = contentService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(contentRepository).findById(id);
    }

    @Test
    @DisplayName("존재하지 않는 ID를 조회하면 ContentNotFoundException을 던진다")
    void throwsWhenContentDoesNotExist() {
        // Given
        Long id = 99L;
        when(contentRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ContentNotFoundException.class, () -> contentService.findById(id));
        verify(contentRepository).findById(id);
    }

    @Test
    @DisplayName("콘텐츠를 수정하면 수정된 데이터를 저장소에 전달하고 반환한다")
    void updateContent() {
        // Given
        Long id = 1L;
        Content existing = new Content(id, "Old title", "Old description");
        Content updated = new Content(id, "New title", "New description");
        when(contentRepository.findById(id)).thenReturn(Optional.of(existing));
        when(contentRepository.update(id, "New title", "New description")).thenReturn(updated);

        // When
        Content result = contentService.update(id, "New title", "New description");

        // Then
        assertNotNull(result);
        assertEquals(updated, result);
        verify(contentRepository).findById(id);
        verify(contentRepository).update(id, "New title", "New description");
    }

    @Test
    @DisplayName("콘텐츠 삭제 시 댓글과 콘텐츠를 저장소에서 삭제한다")
    void deleteContent() {
        // Given
        Long id = 1L;
        when(contentRepository.findById(id))
                .thenReturn(Optional.of(new Content(id, "Title", "Description")));

        // When
        contentService.delete(id);

        // Then
        verify(contentRepository).findById(id);
        verify(commentRepository).deleteByContentId(id);
        verify(contentRepository).deleteById(id);
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠는 댓글이나 콘텐츠를 삭제하지 않는다")
    void doesNotDeleteWhenContentDoesNotExist() {
        // Given
        Long id = 99L;
        when(contentRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ContentNotFoundException.class, () -> contentService.delete(id));
        verify(contentRepository).findById(id);
        verifyNoInteractions(commentRepository);
    }
}
