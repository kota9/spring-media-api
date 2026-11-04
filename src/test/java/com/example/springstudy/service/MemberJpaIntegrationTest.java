package com.example.springstudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.springstudy.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
class MemberJpaIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void createsEntityAndStoresMember() {
        Member transientMember = new Member("홍길동", "transient@example.com");
        assertThat(transientMember.getId()).isNull();

        Long id = memberService.saveMember("홍길동", "save@example.com");

        assertThat(id).isNotNull();
        assertThat(memberService.findMember(id).getEmail()).isEqualTo("save@example.com");
    }

    @Test
    void findsMember() {
        Long id = memberService.saveMember("조회", "find@example.com");

        assertThat(memberService.findMember(id).getName()).isEqualTo("조회");
    }

    @Test
    void detectsEmailChangeAtTransactionCommit() {
        Long id = memberService.saveMember("수정", "before@example.com");

        memberService.updateEmail(id, "after@example.com");

        assertThat(memberService.findMember(id).getEmail()).isEqualTo("after@example.com");
    }

    @Test
    void deletesMember() {
        Long id = memberService.saveMember("삭제", "delete@example.com");

        memberService.deleteMember(id);

        assertThat(memberService.findMember(id)).isNull();
    }

    @Test
    @Transactional
    void firstLevelCacheReturnsSameObjectAndContainsTracksManagedState() {
        Member member = new Member("캐시", "cache@example.com");
        assertThat(entityManager.contains(member)).isFalse();

        entityManager.persist(member);
        assertThat(entityManager.contains(member)).isTrue();

        Member first = entityManager.find(Member.class, member.getId());
        Member second = entityManager.find(Member.class, member.getId());
        assertThat(second).isSameAs(first);

        entityManager.detach(member);
        assertThat(entityManager.contains(member)).isFalse();
        member.changeEmail("detached@example.com");
        entityManager.flush();
        assertThat(jdbcTemplate.queryForObject(
                "select email from members where id = ?", String.class, member.getId()))
                .isEqualTo("cache@example.com");
    }

    @Test
    @Transactional
    void removedEntityIsDeletedWhenFlushedAndFlushKeepsTransactionOpen() {
        Member member = new Member("플러시", "flush@example.com");
        entityManager.persist(member);
        entityManager.flush();
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from members where id = ?", Integer.class, member.getId()))
                .isEqualTo(1);

        entityManager.remove(member);
        assertThat(entityManager.contains(member)).isFalse();
        entityManager.flush();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from members where id = ?", Integer.class, member.getId()))
                .isZero();
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
    }

    @Test
    @Transactional
    void clearDetachesAllEntitiesAndWriteBehindFlushesQueuedChanges() {
        Member first = new Member("지연1", "delay1@example.com");
        Member second = new Member("지연2", "delay2@example.com");
        entityManager.persist(first);
        entityManager.persist(second);
        assertThat(entityManager.contains(first)).isTrue();
        assertThat(entityManager.contains(second)).isTrue();

        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.contains(first)).isFalse();
        assertThat(entityManager.contains(second)).isFalse();
    }

    @Test
    void rollsBackThroughSpringTransactionProxyWhenServiceThrows() {
        assertThatThrownBy(() -> memberService.saveMemberThenFail("롤백", "rollback@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("rollback practice");

        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from members where email = ?", Integer.class, "rollback@example.com"))
                .isZero();
    }
}
