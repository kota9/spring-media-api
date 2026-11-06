package com.example.springstudy.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:entitymapping;DB_CLOSE_DELAY=-1")
@Transactional
class EntityMappingTest {

    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void savesAndReloadsUserAndCategoryWithGeneratedIds() {
        User user = new User("jpa@example.com", "JPA User", "hashed-value", UserRole.ADMIN);
        Category category = new Category("Learning", "JPA practice");
        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.flush();
        Long userId = user.getId();
        Long categoryId = category.getId();
        assertThat(userId).isNotNull();
        assertThat(categoryId).isNotNull();

        entityManager.clear();
        assertThat(entityManager.find(User.class, userId).getEmail()).isEqualTo("jpa@example.com");
        assertThat(entityManager.find(Category.class, categoryId).getName()).isEqualTo("Learning");
    }

    @Test
    void savesContentAndCommentWithStringEnumsAndForeignKeys() {
        User author = new User("author@example.com", "Author", "hashed", UserRole.USER);
        Category category = new Category("Tech", null);
        entityManager.persist(author);
        entityManager.persist(category);
        Content content = new Content("JPA", "Entity mapping", author, category);
        entityManager.persist(content);
        Comment comment = new Comment("Good lesson", author, content);
        entityManager.persist(comment);
        entityManager.flush();

        assertThat(content.getStatus()).isEqualTo(ContentStatus.DRAFT);
        assertThat(jdbcTemplate.queryForObject("select role from users where id = ?", String.class, author.getId()))
                .isEqualTo("USER");
        assertThat(jdbcTemplate.queryForObject("select status from contents where id = ?", String.class, content.getId()))
                .isEqualTo("DRAFT");
        assertThat(jdbcTemplate.queryForObject("select author_id from contents where id = ?", Long.class, content.getId()))
                .isEqualTo(author.getId());
        assertThat(jdbcTemplate.queryForObject("select category_id from contents where id = ?", Long.class, content.getId()))
                .isEqualTo(category.getId());
        assertThat(jdbcTemplate.queryForObject("select author_id from comments where id = ?", Long.class, comment.getId()))
                .isEqualTo(author.getId());
        assertThat(jdbcTemplate.queryForObject("select content_id from comments where id = ?", Long.class, comment.getId()))
                .isEqualTo(content.getId());
    }

    @Test
    void changesManagedEntitiesAndReloadsAfterClear() {
        User author = new User("change@example.com", "Before", "hashed", UserRole.USER);
        Category category = new Category("Before category", null);
        entityManager.persist(author);
        entityManager.persist(category);
        Content content = new Content("Before title", "Before body", author, category);
        entityManager.persist(content);
        Comment comment = new Comment("Before comment", author, content);
        entityManager.persist(comment);
        entityManager.flush();

        author.changeName("After");
        author.changeEmail("changed@example.com");
        category.changeName("After category");
        category.changeDescription("Updated");
        content.changeTitle("After title");
        comment.changeBody("After comment");
        entityManager.flush();
        Long userId = author.getId();
        Long categoryId = category.getId();
        Long contentId = content.getId();
        Long commentId = comment.getId();
        entityManager.clear();

        assertThat(entityManager.find(User.class, userId).getName()).isEqualTo("After");
        assertThat(entityManager.find(User.class, userId).getEmail()).isEqualTo("changed@example.com");
        assertThat(entityManager.find(Category.class, categoryId).getName()).isEqualTo("After category");
        assertThat(entityManager.find(Category.class, categoryId).getDescription()).isEqualTo("Updated");
        assertThat(entityManager.find(Content.class, contentId).getTitle()).isEqualTo("After title");
        assertThat(entityManager.find(Comment.class, commentId).getBody()).isEqualTo("After comment");
    }

    @Test
    void enforcesUniqueAndNotNullConstraints() {
        entityManager.persist(new User("duplicate@example.com", "First", "hashed", UserRole.USER));
        entityManager.flush();
        assertThatThrownBy(() -> entityManager.persist(
                new User("duplicate@example.com", "Second", "hashed", UserRole.USER)))
                .isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    void enforcesRequiredColumns() {
        assertThatThrownBy(() -> entityManager.persist(new User(null, "Missing email", "hashed", UserRole.USER)))
                .isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }
}
