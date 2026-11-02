package com.example.springstudy.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.springstudy.domain.Category;
import com.example.springstudy.dto.CategoryRequest;
import com.example.springstudy.exception.CategoryNotFoundException;
import com.example.springstudy.repository.CategoryRepository;
import com.example.springstudy.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리를 등록하고 저장된 데이터를 반환한다")
    void createCategory() {
        // Given
        CategoryRequest request = new CategoryRequest("Books", "Book reviews");
        Category saved = new Category(1L, request.name(), request.description());
        when(categoryRepository.save(request.name(), request.description())).thenReturn(saved);

        // When
        Category result = categoryService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(saved, result);
        verify(categoryRepository).save(request.name(), request.description());
    }

    @Test
    @DisplayName("ID로 카테고리를 조회하면 해당 데이터를 반환한다")
    void findCategoryById() {
        // Given
        Long categoryId = 1L;
        Category expected = new Category(categoryId, "Books", "Book reviews");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(expected));

        // When
        Category result = categoryService.findById(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리를 조회하면 CategoryNotFoundException을 던진다")
    void throwsWhenCategoryDoesNotExist() {
        // Given
        Long categoryId = 99L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(categoryId));
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("카테고리를 수정하고 수정된 데이터를 반환한다")
    void updateCategory() {
        // Given
        Long categoryId = 1L;
        CategoryRequest request = new CategoryRequest("Updated books", "Updated description");
        Category existing = new Category(categoryId, "Books", "Book reviews");
        Category updated = new Category(categoryId, request.name(), request.description());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));
        when(categoryRepository.update(categoryId, request.name(), request.description())).thenReturn(updated);

        // When
        Category result = categoryService.update(categoryId, request);

        // Then
        assertNotNull(result);
        assertEquals(updated, result);
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).update(categoryId, request.name(), request.description());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리는 수정하지 않고 예외를 던진다")
    void throwsWhenUpdatingMissingCategory() {
        // Given
        Long categoryId = 99L;
        CategoryRequest request = new CategoryRequest("Updated", "Description");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.update(categoryId, request));
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("카테고리를 삭제하면 저장소에 삭제 요청을 전달한다")
    void deleteCategory() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(new Category(categoryId, "Books", "Book reviews")));

        // When
        categoryService.delete(categoryId);

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리는 삭제하지 않고 예외를 던진다")
    void throwsWhenDeletingMissingCategory() {
        // Given
        Long categoryId = 99L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.delete(categoryId));
        verify(categoryRepository).findById(categoryId);
    }
}
