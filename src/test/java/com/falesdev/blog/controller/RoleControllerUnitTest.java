package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.RoleDto;
import com.falesdev.blog.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleControllerUnitTest {

    @InjectMocks
    private RoleController roleController;

    @Mock
    private RoleService roleService;

    private RoleDto roleDto1,roleDto2;

    @BeforeEach
    void setUp() {
        // DTOs to list
        roleDto1 = RoleDto.builder().id(UUID.randomUUID()).name("ADMIN").build();
        roleDto2 = RoleDto.builder().id(UUID.randomUUID()).name("USER").build();
    }

    @Test
    @DisplayName("Success Get Roles")
    void listRoles_ShouldReturnListOfRoles() {
        List<RoleDto> roleList = List.of(roleDto1,roleDto2);
        when(roleService.getAllRoles()).thenReturn(roleList);

        ResponseEntity<List<RoleDto>> response = roleController.getAllRoles();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roleList).hasSize(2);
        verify(roleService).getAllRoles();
    }
}
