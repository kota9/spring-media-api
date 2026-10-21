package com.example.springstudy.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springstudy.domain.Content;
import com.example.springstudy.dto.ContentCreateRequest;
import com.example.springstudy.dto.ContentPatchRequest;
import com.example.springstudy.dto.ContentResponse;
import com.example.springstudy.dto.ContentUpdateRequest;
import com.example.springstudy.exception.InvalidContentPatchException;
import com.example.springstudy.service.ContentService;

@RestController
@RequestMapping("/api/contents")
public class ContentController {
    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public List<ContentResponse> findAll() {
        return contentService.findAll().stream().map(ContentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ContentResponse findById(@PathVariable Long id) {
        return ContentResponse.from(contentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ContentResponse> create(@Valid @RequestBody ContentCreateRequest request) {
        if (request == null) {
            throw new InvalidContentPatchException("Request body must be a JSON object");
        }
        Content created = contentService.create(request.title(), request.description());
        return ResponseEntity.created(URI.create("/api/contents/" + created.id()))
                .body(ContentResponse.from(created));
    }

    @PutMapping("/{id}")
    public ContentResponse update(@PathVariable Long id, @Valid @RequestBody ContentUpdateRequest request) {
        if (request == null) {
            throw new InvalidContentPatchException("Request body must be a JSON object");
        }
        return ContentResponse.from(contentService.update(id, request.title(), request.description()));
    }

    @PatchMapping("/{id}")
    public ContentResponse patch(@PathVariable Long id, @RequestBody ContentPatchRequest request) {
        if (request == null) {
            throw new InvalidContentPatchException("Request body must be a JSON object");
        }
        return ContentResponse.from(contentService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
