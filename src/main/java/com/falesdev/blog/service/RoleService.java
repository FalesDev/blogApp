package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.RoleDto;
import com.falesdev.blog.domain.entity.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {

    List<RoleDto> getAllRoles();
    Set<Role> getRolesByIds(Set<UUID> ids);
}
