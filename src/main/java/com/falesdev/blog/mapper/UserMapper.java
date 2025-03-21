package com.falesdev.blog.mapper;

import com.falesdev.blog.domain.dto.UserDto;
import com.falesdev.blog.domain.dto.request.CreateUserRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateUserRequestDto;
import com.falesdev.blog.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toCreateUser(CreateUserRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(UpdateUserRequestDto dto, @MappingTarget User user);
}
