package com.example.springstudy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.springstudy.domain.Media;

@Repository
public class MediaRepository {

    private final List<Media> media = List.of(
            new Media(1L, "Media 1"),
            new Media(2L, "Media 2"),
            new Media(3L, "Media 3")
    );

    public List<Media> findAll() {
        return media;
    }

    public Optional<Media> findById(Long id) {
        return media.stream().filter(item -> item.id().equals(id)).findFirst();
    }
}
