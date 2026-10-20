package com.example.springstudy.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.springstudy.domain.Media;
import com.example.springstudy.repository.MediaRepository;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public List<Media> findAll() {
        return mediaRepository.findAll();
    }

    public Optional<Media> findById(Long id) {
        return mediaRepository.findById(id);
    }
}
