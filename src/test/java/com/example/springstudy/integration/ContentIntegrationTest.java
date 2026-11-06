package com.example.springstudy.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import com.example.springstudy.domain.Content;
import com.example.springstudy.repository.ContentRepository;
import com.example.springstudy.service.ContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@ActiveProfiles("test")
@Transactional
class ContentIntegrationTest {

    @Autowired
    private ContentService contentService;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    private Long createdContentId;

    @BeforeEach
    void createSchemaAndClearContents() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS contents (
                    id BIGINT PRIMARY KEY,
                    title VARCHAR(100) NOT NULL,
                    description VARCHAR(4000) NOT NULL
                )
                """);
        jdbcTemplate.update("DELETE FROM contents");
    }

    @Test
    @DisplayName("Spring 서비스와 H2 저장소로 콘텐츠를 저장하고 조회한다")
    void createsAndFindsContentInTestDatabase() {
        // Given
        assertNotNull(contentService);
        assertNotNull(contentRepository);
        assertNotNull(dataSource);
        assertTrue(environment.getProperty("spring.datasource.url").startsWith("jdbc:h2:mem:"));

        // When
        Content created = contentService.create("Integration test", "Saved in H2");
        createdContentId = created.id();
        Content found = contentService.findById(createdContentId);

        // Then
        assertEquals("Integration test", created.title());
        assertEquals("Saved in H2", created.description());
        assertEquals(created, found);
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM contents WHERE id = ?", Integer.class, createdContentId));
    }

    @AfterTransaction
    void verifiesTestTransactionWasRolledBack() {
        assertNotNull(createdContentId);
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM contents WHERE id = ?", Integer.class, createdContentId));
    }

    @TestConfiguration
    static class TestDatabaseConfiguration {
        @Bean
        @Primary
        ContentRepository testDatabaseContentRepository(JdbcTemplate jdbcTemplate) {
            return new TestDatabaseContentRepository(jdbcTemplate);
        }
    }

    private static class TestDatabaseContentRepository implements ContentRepository {
        private final JdbcTemplate jdbcTemplate;
        private final AtomicLong idGenerator = new AtomicLong();

        private TestDatabaseContentRepository(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        public List<Content> findAll() {
            return jdbcTemplate.query("SELECT id, title, description FROM contents ORDER BY id",
                    (rs, rowNum) -> new Content(rs.getLong("id"), rs.getString("title"),
                            rs.getString("description")));
        }

        @Override
        public Optional<Content> findById(Long id) {
            List<Content> results = jdbcTemplate.query(
                    "SELECT id, title, description FROM contents WHERE id = ?",
                    (rs, rowNum) -> new Content(rs.getLong("id"), rs.getString("title"),
                            rs.getString("description")), id);
            return results.stream().findFirst();
        }

        @Override
        public Content save(String title, String description) {
            long id = idGenerator.incrementAndGet();
            jdbcTemplate.update("INSERT INTO contents (id, title, description) VALUES (?, ?, ?)",
                    id, title, description);
            return new Content(id, title, description);
        }

        @Override
        public Content update(Long id, String title, String description) {
            jdbcTemplate.update("UPDATE contents SET title = ?, description = ? WHERE id = ?",
                    title, description, id);
            return new Content(id, title, description);
        }

        @Override
        public void deleteById(Long id) {
            jdbcTemplate.update("DELETE FROM contents WHERE id = ?", id);
        }
    }
}
