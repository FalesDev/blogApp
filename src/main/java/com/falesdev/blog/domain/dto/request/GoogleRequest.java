package com.falesdev.blog.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleRequest(
        @NotBlank(message = "Token is required")
        String code
) {
}
