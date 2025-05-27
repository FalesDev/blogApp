package com.falesdev.blog.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

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

    @NotBlank(message = "FirstName is required")
    private String firstName;

    @NotBlank(message = "LastName is required")
    private String lastName;

    @Builder.Default
    @Size(min = 1,message = "At least one role is required")
    private Set<UUID> roleIds = new HashSet<>();

    @URL(protocol = "https", message = "Must be a valid HTTPS URL")
    private String imageURL;
}
