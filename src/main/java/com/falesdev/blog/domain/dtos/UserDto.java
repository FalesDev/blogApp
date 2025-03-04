package com.falesdev.blog.domain.dtos;

import com.falesdev.blog.domain.entities.Post;
import com.falesdev.blog.domain.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private UUID id;
    private String email;
    private String password;
    private String name;
    private Set<RoleDto> roles;
    private LocalDateTime createdAt;
}
