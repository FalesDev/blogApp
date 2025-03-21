package com.falesdev.blog.service.impl;

import com.falesdev.blog.domain.dto.UserDto;
import com.falesdev.blog.domain.dto.request.CreateUserRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateUserRequestDto;
import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.mapper.UserMapper;
import com.falesdev.blog.repository.UserRepository;
import com.falesdev.blog.service.RoleService;
import com.falesdev.blog.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequestDto userRequestDto) {
        if (userRepository.existsByEmailIgnoreCase(userRequestDto.getEmail())){
            throw new IllegalArgumentException("User already exists with email: " + userRequestDto.getName());
        }

        User newUser = userMapper.toCreateUser(userRequestDto);
        newUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));

        Set<Role> roles = roleService.getRolesByIds(userRequestDto.getRoleIds());
        newUser.setRoles(roles);
        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequestDto updateUserRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("User does not exist with id "+id));
        userMapper.updateFromDto(updateUserRequestDto, existingUser);

        if (updateUserRequestDto.getRoleIds() != null && !updateUserRequestDto.getRoleIds().isEmpty()) {
            Set<Role> validRoles = roleService.getRolesByIds(updateUserRequestDto.getRoleIds());
            existingUser.setRoles(validRoles);
        }

        if (updateUserRequestDto.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(updateUserRequestDto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("User does not exist with ID "+id));
        userRepository.delete(user);
    }
}
