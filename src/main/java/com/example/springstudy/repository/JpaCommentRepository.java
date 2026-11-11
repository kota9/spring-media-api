package com.example.springstudy.repository;

import com.example.springstudy.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** JPA 성능 실습 전용 Comment 조회 Repository. */
public interface JpaCommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c")
    List<Comment> findAllLazyForLab();

    @Query("select c from Comment c join fetch c.content")
    List<Comment> findAllWithContentFetchJoin();

    @EntityGraph(attributePaths = "content")
    @Query("select c from Comment c")
    List<Comment> findAllWithContentEntityGraph();
}
