package com.falesdev.blog.services.impl;

import com.falesdev.blog.domain.dtos.RoleDto;
import com.falesdev.blog.domain.entities.Role;
import com.falesdev.blog.mappers.RoleMapper;
import com.falesdev.blog.respositories.RoleRepository;
import com.falesdev.blog.services.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    public Role getRoleById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id));
    }

    @Override
    public Set<Role> getRolesByIds(Set<UUID> ids) {
        List<Role> foundRoles = roleRepository.findAllById(ids);
        if(foundRoles.size() != ids.size()) {
            throw new EntityNotFoundException("Not all specified Role IDs exist");
        }
        return new HashSet<>(foundRoles);
    }
}
