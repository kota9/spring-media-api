package com.example.springstudy.integration;

import com.example.springstudy.domain.Comment;
import com.example.springstudy.domain.Category;
import com.example.springstudy.domain.Content;
import com.example.springstudy.domain.User;
import com.example.springstudy.repository.CategoryRepository;
import com.example.springstudy.repository.CommentRepository;
import com.example.springstudy.repository.ContentRepository;
import com.example.springstudy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Set RUN_MYSQL_INTEGRATION_TESTS=true and DB_PASSWORD to run against local MySQL. */
@SpringBootTest
@ActiveProfiles("mysql")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
class MySqlPersistenceIntegrationTest {

    @Autowired UserRepository users;
    @Autowired ContentRepository contents;
    @Autowired CommentRepository comments;
    @Autowired CategoryRepository categories;

    @Test
    void storesCrudDataAndRelationsInMysql() {
        String suffix = UUID.randomUUID().toString();
        User user = users.save("MySQL test", "mysql-test-" + suffix + "@example.com");
        Category category = categories.save("MySQL test " + suffix, "Persistence test category");
        Content content = contents.save(
                "MySQL persistence", "Persisted description " + suffix, user.id(), category.id());
        Comment comment = null;
        try {
            comment = comments.save(content.id(), user.id(), "Persisted comment " + suffix);

            assertThat(users.findById(user.id())).contains(user);
            assertThat(categories.findById(category.id())).contains(category);
            assertThat(contents.findById(content.id())).contains(content);
            assertThat(comments.findByContentId(content.id())).contains(comment);

            Content updated = contents.update(content.id(), "Updated title", "Updated description");
            assertThat(contents.findById(content.id())).contains(updated);

            comments.deleteById(comment.id());
            assertThat(comments.findById(comment.id())).isEmpty();
        } finally {
            comments.deleteByContentId(content.id());
            contents.deleteById(content.id());
            users.deleteById(user.id());
            categories.deleteById(category.id());
        }
    }
}
