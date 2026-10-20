package com.example.springstudy.dto;

import com.example.springstudy.domain.Media;

public record MediaResponse(Long id, String title) {

    public static MediaResponse from(Media media) {
        return new MediaResponse(media.id(), media.title());
    }
}
