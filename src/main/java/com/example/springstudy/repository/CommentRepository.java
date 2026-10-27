package com.example.springstudy.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.example.springstudy.domain.Comment;

@Repository
public class CommentRepository {
    private final ConcurrentHashMap<Long, Comment> comments = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public List<Comment> findByContentId(Long contentId) {
        return comments.values().stream()
                .filter(comment -> comment.contentId().equals(contentId))
                .sorted(Comparator.comparing(Comment::id))
                .toList();
    }

    public Optional<Comment> findById(Long commentId) {
        return Optional.ofNullable(comments.get(commentId));
    }

    public Comment save(Long contentId, Long userId, String body) {
        Comment comment = new Comment(idGenerator.incrementAndGet(), contentId, userId, body);
        comments.put(comment.id(), comment);
        return comment;
    }

    public Comment update(Long commentId, String body) {
        Comment existing = comments.get(commentId);
        Comment updated = new Comment(commentId, existing.contentId(), existing.userId(), body);
        comments.put(commentId, updated);
        return updated;
    }

    public void deleteById(Long commentId) {
        comments.remove(commentId);
    }

    public void deleteByContentId(Long contentId) {
        comments.forEach((commentId, comment) -> {
            if (comment.contentId().equals(contentId)) {
                comments.remove(commentId, comment);
            }
        });
    }

    public void deleteByUserId(Long userId) {
        comments.forEach((commentId, comment) -> {
            if (comment.userId().equals(userId)) {
                comments.remove(commentId, comment);
            }
        });
    }
}
