package com.example.springstudy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springstudy.domain.Content;
import com.example.springstudy.dto.ContentPatchRequest;
import com.example.springstudy.exception.ContentNotFoundException;
import com.example.springstudy.exception.InvalidContentPatchException;
import com.example.springstudy.repository.CommentRepository;
import com.example.springstudy.repository.ContentRepository;

@Service
public class ContentService {
    private final ContentRepository contentRepository;
    private final CommentRepository commentRepository;

    public ContentService(ContentRepository contentRepository, CommentRepository commentRepository) {
        this.contentRepository = contentRepository;
        this.commentRepository = commentRepository;
    }

    public List<Content> findAll() {
        return contentRepository.findAll();
    }

    public Content findById(Long id) {
        return contentRepository.findById(id).orElseThrow(() -> new ContentNotFoundException(id));
    }

    public Content create(String title, String description) {
        return contentRepository.save(title, description);
    }

    public Content update(Long id, String title, String description) {
        findById(id);
        return contentRepository.update(id, title, description);
    }

    public Content patch(Long id, ContentPatchRequest request) {
        Content existing = findById(id);
        if (!request.hasTitle() && !request.hasDescription()) {
            throw new InvalidContentPatchException("수정할 항목을 하나 이상 입력해야 합니다.");
        }

        String title = existing.title();
        String description = existing.description();
        if (request.hasTitle()) {
            if (!(request.getTitle() instanceof String patchedTitle)
                    || patchedTitle.isBlank() || patchedTitle.length() > 100) {
                throw new InvalidContentPatchException("제목은 필수이며 100자 이하여야 합니다.");
            }
            title = patchedTitle;
        }
        if (request.hasDescription()) {
            if (!(request.getDescription() instanceof String patchedDescription) || patchedDescription.isBlank()) {
                throw new InvalidContentPatchException("내용은 문자열이어야 하며 빈 값일 수 없습니다.");
            }
            description = patchedDescription;
        }
        return contentRepository.update(id, title, description);
    }

    public void delete(Long id) {
        findById(id);
        commentRepository.deleteByContentId(id);
        contentRepository.deleteById(id);
    }
}
