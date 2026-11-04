package com.example.springstudy.service;

import com.example.springstudy.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long saveMember(String name, String email) {
        Member member = new Member(name, email);
        entityManager.persist(member);
        return member.getId();
    }

    @Transactional(readOnly = true)
    public Member findMember(Long id) {
        return entityManager.find(Member.class, id);
    }

    @Transactional
    public void updateEmail(Long id, String email) {
        Member member = entityManager.find(Member.class, id);
        if (member != null) {
            member.changeEmail(email);
        }
    }

    @Transactional
    public void deleteMember(Long id) {
        Member member = entityManager.find(Member.class, id);
        if (member != null) {
            entityManager.remove(member);
        }
    }

    @Transactional
    public void saveMemberThenFail(String name, String email) {
        entityManager.persist(new Member(name, email));
        throw new IllegalStateException("rollback practice");
    }
}
