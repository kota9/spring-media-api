package com.example.springstudy.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 미디어 관련 HTTP 요청을 받는 Controller.
 * Day 19에서는 Service/DTO 없이 Controller 안에서 직접 응답을 만든다.
 * 이후 비즈니스 로직은 service, 응답 객체는 dto 패키지로 옮길 예정.
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    // GET /api/media -> 미디어 제목 목록
    @GetMapping
    public List<String> getMediaList() {
        return List.of("Inception", "Interstellar", "The Dark Knight");
    }

    // GET /api/media/{id} -> 단건 조회
    @GetMapping("/{id}")
    public Map<String, Object> getMedia(@PathVariable Long id) {
        return Map.of(
                "id", id,
                "title", "Media " + id
        );
    }
}
