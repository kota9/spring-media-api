package com.example.springstudy.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springstudy.dto.CommentCreateRequest;
import com.example.springstudy.dto.CommentResponse;
import com.example.springstudy.dto.CommentUpdateRequest;
import com.example.springstudy.service.CommentService;

@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/contents/{contentId}/comments")
    public List<CommentResponse> findByContentId(@PathVariable Long contentId) {
        return commentService.findByContentId(contentId).stream().map(CommentResponse::from).toList();
    }

    @PostMapping("/api/contents/{contentId}/comments")
    public ResponseEntity<CommentResponse> create(
            @PathVariable Long contentId, @Valid @RequestBody CommentCreateRequest request) {
        var created = commentService.create(contentId, request);
        return ResponseEntity.created(URI.create("/api/comments/" + created.id()))
                .body(CommentResponse.from(created));
    }

    @GetMapping("/api/comments/{commentId}")
    public CommentResponse findById(@PathVariable Long commentId) {
        return CommentResponse.from(commentService.findById(commentId));
    }

    @PutMapping("/api/comments/{commentId}")
    public CommentResponse update(
            @PathVariable Long commentId, @Valid @RequestBody CommentUpdateRequest request) {
        return CommentResponse.from(commentService.update(commentId, request));
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}
