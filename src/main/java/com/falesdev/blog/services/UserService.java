package com.falesdev.blog.services;

import com.falesdev.blog.domain.dtos.UserDto;
import com.falesdev.blog.domain.dtos.requests.CreateUserRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdateUserRequestDto;
import com.falesdev.blog.domain.entities.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDto> getAllUsers();
    UserDto getUserById(UUID id);
    UserDto createUser(CreateUserRequestDto userDto);
    UserDto updateUser(UUID id, UpdateUserRequestDto userDto);
    void deleteUser(UUID id);
}
