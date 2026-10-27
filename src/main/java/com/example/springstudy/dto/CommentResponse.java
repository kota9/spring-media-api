package com.example.springstudy.dto;

import com.example.springstudy.domain.Comment;

public record CommentResponse(Long id, Long contentId, Long userId, String body) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.id(), comment.contentId(), comment.userId(), comment.body());
    }
}
