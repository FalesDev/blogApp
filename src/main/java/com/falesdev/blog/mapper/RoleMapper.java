package com.falesdev.blog.mapper;

import com.falesdev.blog.domain.dto.RoleDto;
import com.falesdev.blog.domain.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    RoleDto toDto(Role role);
}
