package com.falesdev.blog.service.impl;

import com.falesdev.blog.domain.dto.RoleDto;
import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.mapper.RoleMapper;
import com.falesdev.blog.repository.RoleRepository;
import com.falesdev.blog.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Role> getRolesByIds(Set<UUID> ids) {
        List<Role> foundRoles = roleRepository.findAllById(ids);
        if(foundRoles.size() != ids.size()) {
            throw new EntityNotFoundException("Not all specified Role IDs exist");
        }
        return new HashSet<>(foundRoles);
    }
}
