package com.falesdev.blog.domain.dto.response;

import com.falesdev.blog.domain.dto.RoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthUserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<RoleDto> roles;
    private String imageURL;
}
