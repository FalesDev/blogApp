package com.falesdev.blog.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {

    @NotNull(message = "User ID is required")
    private UUID id;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, max = 20, message = "Password must be between {min} and {max} characters")
    private String password;

    @Size(min = 1, message = "Name cannot be empty")
    private String name;

    @Builder.Default
    @Size(min = 1,message = "At least one role is required")
    private Set<UUID> roleIds = new HashSet<>();
}
