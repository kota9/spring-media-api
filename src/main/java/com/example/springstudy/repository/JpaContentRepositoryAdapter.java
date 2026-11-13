package com.example.springstudy.repository;

import com.example.springstudy.domain.Content;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaContentRepositoryAdapter implements ContentRepository {
    private final JpaContentRepository contents;

    public JpaContentRepositoryAdapter(JpaContentRepository contents) {
        this.contents = contents;
    }

    @Override
    public List<Content> findAll() {
        return contents.findAllOrdered().stream().map(JpaContentRepositoryAdapter::toDomain).toList();
    }

    @Override
    public Optional<Content> findById(Long id) {
        return contents.findById(id).map(JpaContentRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional
    public Content save(String title, String description) {
        return toDomain(contents.save(new com.example.springstudy.entity.Content(title, description)));
    }

    @Override
    @Transactional
    public Content update(Long id, String title, String description) {
        com.example.springstudy.entity.Content content = contents.findById(id).orElseThrow();
        content.changeTitle(title);
        content.changeBody(description);
        return toDomain(content);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        contents.deleteById(id);
    }

    private static Content toDomain(com.example.springstudy.entity.Content content) {
        return new Content(content.getId(), content.getTitle(), content.getBody());
    }
}
