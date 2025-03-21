package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.UserDto;
import com.falesdev.blog.domain.dto.request.CreateUserRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateUserRequestDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDto> getAllUsers();
    UserDto getUserById(UUID id);
    UserDto createUser(CreateUserRequestDto userDto);
    UserDto updateUser(UUID id, UpdateUserRequestDto userDto);
    void deleteUser(UUID id);
}
