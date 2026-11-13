package com.example.springstudy.repository;

import com.example.springstudy.domain.Comment;
import com.example.springstudy.entity.Content;
import com.example.springstudy.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class CommentRepository {
    private final JpaCommentRepository comments;
    private final JpaContentRepository contents;
    private final JpaUserRepository users;

    public CommentRepository(JpaCommentRepository comments, JpaContentRepository contents, JpaUserRepository users) {
        this.comments = comments;
        this.contents = contents;
        this.users = users;
    }

    public List<Comment> findByContentId(Long contentId) {
        return comments.findByContent_IdOrderByIdAsc(contentId).stream()
                .map(CommentRepository::toDomain).toList();
    }

    public Optional<Comment> findById(Long id) {
        return comments.findById(id).map(CommentRepository::toDomain);
    }

    @Transactional
    public Comment save(Long contentId, Long userId, String body) {
        Content content = contents.getReferenceById(contentId);
        User user = users.getReferenceById(userId);
        return toDomain(comments.save(new com.example.springstudy.entity.Comment(body, user, content)));
    }

    @Transactional
    public Comment update(Long id, String body) {
        com.example.springstudy.entity.Comment comment = comments.findById(id).orElseThrow();
        comment.changeBody(body);
        return toDomain(comment);
    }

    @Transactional
    public void deleteById(Long id) {
        comments.deleteById(id);
    }

    @Transactional
    public void deleteByContentId(Long contentId) {
        comments.deleteForContent(contentId);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        comments.deleteForUser(userId);
    }

    private static Comment toDomain(com.example.springstudy.entity.Comment comment) {
        return new Comment(comment.getId(), comment.getContent().getId(), comment.getAuthor().getId(),
                comment.getBody());
    }
}
