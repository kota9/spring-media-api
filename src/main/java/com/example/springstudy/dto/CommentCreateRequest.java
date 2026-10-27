package com.example.springstudy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotNull(message = "작성자 ID는 필수입니다.") Long userId,
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글은 1000자 이하여야 합니다.") String body) {
}
