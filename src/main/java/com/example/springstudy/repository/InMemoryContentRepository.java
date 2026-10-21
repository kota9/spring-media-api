package com.example.springstudy.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.example.springstudy.domain.Content;

@Repository
public class InMemoryContentRepository implements ContentRepository {
    private final ConcurrentHashMap<Long, Content> contents = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    @Override
    public List<Content> findAll() {
        return contents.values().stream().sorted(Comparator.comparing(Content::id)).toList();
    }

    @Override
    public Optional<Content> findById(Long id) {
        return Optional.ofNullable(contents.get(id));
    }

    @Override
    public Content save(String title, String description) {
        Content content = new Content(idGenerator.incrementAndGet(), title, description);
        contents.put(content.id(), content);
        return content;
    }

    @Override
    public Content update(Long id, String title, String description) {
        Content content = new Content(id, title, description);
        contents.put(id, content);
        return content;
    }

    @Override
    public void deleteById(Long id) {
        contents.remove(id);
    }
}
