package com.example.springstudy.entity;

import com.example.springstudy.repository.JpaContentRepository;
import com.example.springstudy.repository.JpaCommentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** SQL 횟수는 Hibernate Statistics로 검증하고, 실행 로그는 application.yml의 Hibernate SQL 로거로 확인한다. */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class JpaFetchPerformanceTest {

    @Autowired EntityManager entityManager;
    @Autowired JpaContentRepository contentRepository;
    @Autowired JpaCommentRepository commentRepository;
    @Autowired EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        User author = new User("nplus@example.com", "N+1 Author", "hashed", UserRole.USER);
        Category category = new Category("N+1 Category", "JPA fetching lab");
        entityManager.persist(author);
        entityManager.persist(category);

        for (int i = 1; i <= 3; i++) {
            Content content = new Content("Content " + i, "Body " + i, author, category);
            entityManager.persist(content);
            entityManager.persist(new Comment("Comment A " + i, author, content));
            entityManager.persist(new Comment("Comment B " + i, author, content));
        }
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void lazyCollectionCausesNPlusOneAndFetchJoinRemovesIt() {
        statistics.clear();
        List<Comment> comments = commentRepository.findAllLazyForLab();
        int lazyTitleLength = comments.stream().mapToInt(c -> c.getContent().getTitle().length()).sum();
        long lazyQueryCount = statistics.getPrepareStatementCount();

        assertThat(comments).hasSize(6);
        assertThat(lazyTitleLength).isPositive();
        assertThat(lazyQueryCount).isEqualTo(4); // Comment 목록 1회 + 참조된 Content별 조회 3회

        entityManager.clear();
        statistics.clear();
        List<Comment> fetched = commentRepository.findAllWithContentFetchJoin();
        long fetchJoinQueries = statistics.getPrepareStatementCount();

        assertThat(fetched).hasSize(6);
        assertThat(fetched).allSatisfy(c -> assertThat(c.getContent().getTitle()).isNotBlank());
        assertThat(fetchJoinQueries).isEqualTo(1);
    }

    @Test
    void entityGraphAndBatchSizeLoadCollectionsInFewerQueries() {
        statistics.clear();
        List<Comment> graphed = commentRepository.findAllWithContentEntityGraph();
        long graphQueries = statistics.getPrepareStatementCount();
        assertThat(graphed.stream().map(c -> c.getContent().getTitle()).distinct()).hasSize(3);
        graphQueries = statistics.getPrepareStatementCount();
        assertThat(graphQueries).isEqualTo(1);

        entityManager.clear();
        statistics.clear();
        List<Content> batched = contentRepository.findAllLazyForLab();
        long batchQueries = statistics.getPrepareStatementCount();
        assertThat(batched.stream().mapToInt(c -> c.getComments().size()).sum()).isEqualTo(6);
        batchQueries = statistics.getPrepareStatementCount();
        assertThat(batchQueries).isEqualTo(2); // 목록 1회 + @BatchSize 적용한 IN 조회 1회
    }

    @Test
    void collectionFetchJoinWithPageableIsExecutedForPaginationLab() {
        Page<Content> page = contentRepository.findPageWithCommentsFetchJoin(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).allSatisfy(c -> assertThat(c.getComments()).hasSize(2));
    }
}
