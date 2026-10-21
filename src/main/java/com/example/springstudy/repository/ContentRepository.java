package com.example.springstudy.repository;

import java.util.List;
import java.util.Optional;

import com.example.springstudy.domain.Content;

public interface ContentRepository {
    List<Content> findAll();
    Optional<Content> findById(Long id);
    Content save(String title, String description);
    Content update(Long id, String title, String description);
    void deleteById(Long id);
}
