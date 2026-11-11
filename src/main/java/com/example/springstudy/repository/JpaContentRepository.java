package com.example.springstudy.repository;

import com.example.springstudy.entity.Content;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** JPA 성능 실습 전용 조회 Repository. 운영 API의 인메모리 ContentRepository와 분리한다. */
public interface JpaContentRepository extends JpaRepository<Content, Long> {

    @Query("select distinct c from Content c left join fetch c.comments")
    List<Content> findAllWithCommentsFetchJoin();

    @Query("select c from Content c")
    List<Content> findAllLazyForLab();

    @EntityGraph(attributePaths = "comments")
    @Query("select c from Content c")
    List<Content> findAllWithCommentsEntityGraph();

    @Query("select distinct c from Content c left join fetch c.comments")
    Page<Content> findPageWithCommentsFetchJoin(Pageable pageable);
}
