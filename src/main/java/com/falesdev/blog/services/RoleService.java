package com.falesdev.blog.services;

import com.falesdev.blog.domain.entities.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {

    List<Role> getAllRoles();
    Role getRoleById(UUID id);
    Set<Role> getRolesByIds(Set<UUID> ids);
}
