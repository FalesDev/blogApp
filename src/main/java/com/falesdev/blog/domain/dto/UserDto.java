package com.falesdev.blog.domain.dto;

import com.falesdev.blog.domain.RegisterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private String firstName;
    private String lastName;
    private Set<RoleDto> roles;
    private String imageURL;
    private RegisterType registerType;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
