package com.falesdev.blog.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "LoginRequest", description = "Payload for user authentication")
public class LoginRequest {

    @Schema(description = "User's email", example = "admin@test.com", requiredMode = REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "User's password", example = "adminpassword", requiredMode = REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between {min} and {max} characters")
    private String password;
}
