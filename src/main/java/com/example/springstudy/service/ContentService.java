package com.example.springstudy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springstudy.domain.Content;
import com.example.springstudy.dto.ContentPatchRequest;
import com.example.springstudy.exception.ContentNotFoundException;
import com.example.springstudy.exception.InvalidContentPatchException;
import com.example.springstudy.repository.ContentRepository;

@Service
public class ContentService {
    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
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
            throw new InvalidContentPatchException("At least one field must be provided");
        }

        String title = existing.title();
        String description = existing.description();
        if (request.hasTitle()) {
            if (!(request.getTitle() instanceof String patchedTitle) || patchedTitle.isBlank()) {
                throw new InvalidContentPatchException("title must be a non-blank string and cannot be null");
            }
            title = patchedTitle;
        }
        if (request.hasDescription()) {
            if (!(request.getDescription() instanceof String patchedDescription)) {
                throw new InvalidContentPatchException("description must be a string and cannot be null");
            }
            description = patchedDescription;
        }
        return contentRepository.update(id, title, description);
    }

    public void delete(Long id) {
        findById(id);
        contentRepository.deleteById(id);
    }
}
