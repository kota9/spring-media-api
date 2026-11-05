package com.example.springstudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.springstudy.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class MemberPersistenceContextTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void testFirstLevelCache() {
        Long id = createMember("cache");
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Member first = entityManager.find(Member.class, id);
            Member second = entityManager.find(Member.class, id);
            assertThat(second).isSameAs(first);
        });
    }

    @Test
    void testClearCache() {
        Long id = createMember("clear");
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Member first = entityManager.find(Member.class, id);
            entityManager.clear();
            Member second = entityManager.find(Member.class, id);
            assertThat(second).isNotSameAs(first);
        });
    }

    @Test
    void testDirtyChecking() {
        Long id = createMember("dirty");
        String updated = uniqueEmail("updated");
        memberService.updateEmail(id, updated);
        assertThat(emailById(id)).isEqualTo(updated);
    }

    @Test
    void testNoChange() {
        Long id = createMember("nochange");
        String original = emailById(id);
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Member member = entityManager.find(Member.class, id);
            member.changeEmail(member.getEmail());
        });
        assertThat(emailById(id)).isEqualTo(original);
    }

    @Test
    void testDetachedEntity() {
        Long id = createMember("detached");
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Member member = entityManager.find(Member.class, id);
            assertThat(entityManager.contains(member)).isTrue();
            entityManager.detach(member);
            assertThat(entityManager.contains(member)).isFalse();
            member.changeEmail("detached@example.com");
        });
        assertThat(emailById(id)).isNotEqualTo("detached@example.com");
    }

    @Test
    void testFlush() {
        String email = uniqueEmail("flush");
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Member member = new Member("Flush practice", email);
            entityManager.persist(member);
            entityManager.flush();
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from members where email = ?", Integer.class, email)).isEqualTo(1);
        });
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from members where email = ?", Integer.class, email)).isEqualTo(1);
    }

    @Test
    void testFlushAndRollback() {
        String email = uniqueEmail("rollback");
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            entityManager.persist(new Member("Rollback practice", email));
            entityManager.flush();
            throw new IllegalStateException("intentional rollback after flush");
        })).isInstanceOf(IllegalStateException.class)
                .hasMessage("intentional rollback after flush");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from members where email = ?", Integer.class, email)).isZero();
    }

    @Test
    void testAutoFlush() {
        Long id = createMember("autoflush");
        String updated = uniqueEmail("autoflush-updated");
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Member member = entityManager.find(Member.class, id);
            member.changeEmail(updated);
            var result = entityManager.createQuery(
                            "select m from Member m where m.email = :email", Member.class)
                    .setParameter("email", updated)
                    .getResultList();
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isSameAs(member);
        });
        assertThat(emailById(id)).isEqualTo(updated);
    }

    private Long createMember(String prefix) {
        return memberService.saveMember("Persistence " + prefix, uniqueEmail(prefix));
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.com";
    }

    private String emailById(Long id) {
        return jdbcTemplate.queryForObject("select email from members where id = ?", String.class, id);
    }
}
