package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.RoleDto;
import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.mapper.RoleMapper;
import com.falesdev.blog.repository.RoleRepository;
import com.falesdev.blog.service.impl.RoleServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplUnitTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role roleUser;
    private Role roleAdmin;
    private RoleDto roleDtoUser;
    private RoleDto roleDtoAdmin;

    @BeforeEach
    public void setUp() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        roleUser = Role.builder().id(id1).name("USER").build();
        roleAdmin = Role.builder().id(id2).name("ADMIN").build();

        roleDtoUser = RoleDto.builder().id(id1).name("USER").build();
        roleDtoAdmin = RoleDto.builder().id(id2).name("ADMIN").build();
    }

    @Test
    @DisplayName("List all roles - Success")
    void listRoles_ReturnsRoleDtoList() {
        // Arrange
        List<Role> roles = List.of(roleUser, roleAdmin);
        when(roleRepository.findAll()).thenReturn(roles);
        when(roleMapper.toDto(roleUser)).thenReturn(roleDtoUser);
        when(roleMapper.toDto(roleAdmin)).thenReturn(roleDtoAdmin);

        // Act
        List<RoleDto> result = roleService.getAllRoles();

        // Assert
        assertThat(result)
                .hasSize(2)
                .containsExactly(roleDtoUser, roleDtoAdmin);

        verify(roleRepository).findAll();
        verify(roleMapper, times(2)).toDto(any(Role.class));
    }

    @Test
    @DisplayName("Get roles by existing ID - Success")
    void getRolesById_ValidId_ReturnsRolesDto() {
        Set<UUID> ids = Set.of(roleUser.getId(), roleAdmin.getId());
        List<Role> foundRoles = List.of(roleUser, roleAdmin);

        when(roleRepository.findAllById(eq(ids))).thenReturn(foundRoles);

        Set<Role> result = roleService.getRolesByIds(ids);

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(roleUser, roleAdmin);

        verify(roleRepository).findAllById(eq(ids));
    }

    @Test
    @DisplayName("Getting roles with missing IDs - Throws exception")
    void getRolesByIds_MissingIds_ThrowsException() {
        Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());
        when(roleRepository.findAllById(eq(ids))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> roleService.getRolesByIds(ids))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Not all specified Role IDs exist");

        verify(roleRepository).findAllById(eq(ids));
    }

    @Test
    @DisplayName("Get roles with partial missing IDs - Throws exception")
    void getRolesByIds_PartialMissingIds_ThrowsException() {
        Set<UUID> ids = Set.of(roleUser.getId(), UUID.randomUUID());
        when(roleRepository.findAllById(eq(ids))).thenReturn(List.of(roleUser));

        assertThatThrownBy(() -> roleService.getRolesByIds(ids))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Not all specified Role IDs exist");
    }
}
