package com.example.springstudy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springstudy.domain.Comment;
import com.example.springstudy.dto.CommentCreateRequest;
import com.example.springstudy.dto.CommentUpdateRequest;
import com.example.springstudy.exception.CommentNotFoundException;
import com.example.springstudy.repository.CommentRepository;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final ContentService contentService;
    private final UserService userService;

    public CommentService(
            CommentRepository commentRepository, ContentService contentService, UserService userService) {
        this.commentRepository = commentRepository;
        this.contentService = contentService;
        this.userService = userService;
    }

    public List<Comment> findByContentId(Long contentId) {
        contentService.findById(contentId);
        return commentRepository.findByContentId(contentId);
    }

    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    public Comment create(Long contentId, CommentCreateRequest request) {
        contentService.findById(contentId);
        userService.findById(request.userId());
        return commentRepository.save(contentId, request.userId(), request.body());
    }

    public Comment update(Long commentId, CommentUpdateRequest request) {
        findById(commentId);
        return commentRepository.update(commentId, request.body());
    }

    public void delete(Long commentId) {
        findById(commentId);
        commentRepository.deleteById(commentId);
    }
}
