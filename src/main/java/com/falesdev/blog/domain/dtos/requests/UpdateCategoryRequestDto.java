package com.falesdev.blog.domain.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequestDto {

    @NotNull(message = "Category ID is required")
    private UUID id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between {min} and {max} characters")
    @Pattern(regexp = "^[\\w\\s-]+$", message = "Category name can only contain letters, numbers, spaces and hyphens")
    private String name;
}
