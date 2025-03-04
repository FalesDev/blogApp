package com.falesdev.blog.domain.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
public class CreateUserRequestDto {

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, max = 20, message = "Password must be between {min} and {max} characters")
    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    @Builder.Default
    @NotEmpty(message = "At least one role is required")
    private Set<UUID> roleIds = new HashSet<>();
}
