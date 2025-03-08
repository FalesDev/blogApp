package com.falesdev.blog.mappers;

import com.falesdev.blog.domain.dtos.PostDto;
import com.falesdev.blog.domain.dtos.RoleDto;
import com.falesdev.blog.domain.entities.Post;
import com.falesdev.blog.domain.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    RoleDto toDto(Role role);
}
