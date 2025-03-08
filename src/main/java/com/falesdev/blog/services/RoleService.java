package com.falesdev.blog.services;

import com.falesdev.blog.domain.dtos.RoleDto;
import com.falesdev.blog.domain.entities.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {

    List<RoleDto> getAllRoles();
    Role getRoleById(UUID id);
    Set<Role> getRolesByIds(Set<UUID> ids);
}
