package com.falesdev.blog.mappers;

import com.falesdev.blog.domain.dtos.UserDto;
import com.falesdev.blog.domain.dtos.requests.CreateUserRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdateUserRequestDto;
import com.falesdev.blog.domain.entities.User;
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
