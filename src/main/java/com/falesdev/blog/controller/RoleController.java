package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.RoleDto;
import com.falesdev.blog.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role", description = "Controller for Roles")
public class RoleController {

    private final RoleService roleService;

    @Operation(
            summary = "Get all roles",
            description = "Returns a list of all roles"
    )
    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
}
