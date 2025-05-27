package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.UserDto;
import com.falesdev.blog.domain.dto.request.CreateUserRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateUserRequestDto;
import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.mapper.UserMapper;
import com.falesdev.blog.repository.UserRepository;
import com.falesdev.blog.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;
    private CreateUserRequestDto createRequest;
    private UpdateUserRequestDto updateRequest;
    private final UUID userId = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .firstName("Fabricio")
                .lastName("Rodriguez")
                .email("fabricio-1998-xd@hotmail.com")
                .password("encodedPassword")
                .roles(new HashSet<>())
                .build();

        userDto = UserDto.builder()
                .id(userId)
                .firstName("Fabricio")
                .lastName("Rodriguez")
                .email("fabricio-1998-xd@hotmail.com")
                .build();

        createRequest = CreateUserRequestDto.builder()
                .firstName("Fabricio")
                .lastName("Rodriguez")
                .email("fabricio-1998-xd@hotmail.com")
                .password("password")
                .roleIds(Set.of(UUID.randomUUID()))
                .build();

        updateRequest = UpdateUserRequestDto.builder()
                .firstName("Fabricio")
                .lastName("Updated")
                .password("newPassword")
                .roleIds(Set.of(UUID.randomUUID()))
                .build();
    }

    @Test
    @DisplayName("Get all users - Success")
    void getAllUsers_ReturnsUserList() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.getAllUsers();

        assertThat(result)
                .hasSize(1)
                .containsExactly(userDto);
        verify(userRepository).findAll();
        verify(userMapper).toDto(eq(user));
    }

    @Test
    @DisplayName("Get user by valid ID - Success")
    void getUserById_ValidId_ReturnsUserDto() {
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(userId);

        assertThat(result).isEqualTo(userDto);
        verify(userRepository).findById(eq(userId));
    }

    @Test
    @DisplayName("Get user by invalid ID - Throws Exception")
    void getUserById_InvalidId_ThrowsException() {
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(eq(invalidId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(invalidId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(eq(invalidId));
    }

    @Test
    @DisplayName("Create user with existing email - Throws Exception")
    void createUser_ExistingEmail_ThrowsException() {
        when(userRepository.existsByEmailIgnoreCase(eq(createRequest.getEmail()))).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User already exists");

        verify(userRepository).existsByEmailIgnoreCase(eq(createRequest.getEmail()));
    }

    @Test
    @DisplayName("Create new user - Success")
    void createUser_NewUser_ReturnsUserDto() {
        Set<Role> roles = Set.of(Role.builder().id(UUID.randomUUID()).name("USER").build());
        User newUser = User.builder().email(createRequest.getEmail()).build();
        User savedUser = User.builder().id(userId).build();

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(eq(createRequest.getPassword()))).thenReturn("encodedPassword");
        when(roleService.getRolesByIds(eq(createRequest.getRoleIds()))).thenReturn(new HashSet<>(roles));
        when(userMapper.toCreateUser(eq(createRequest))).thenReturn(newUser);
        when(userRepository.save(eq(newUser))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(userDto);

        UserDto result = userService.createUser(createRequest);

        assertThat(result).isEqualTo(userDto);
        verify(passwordEncoder).encode(eq(createRequest.getPassword()));
        verify(roleService).getRolesByIds(eq(createRequest.getRoleIds()));
        verify(userMapper).toCreateUser(eq(createRequest));
        verify(userRepository).save(eq(newUser));

    }

    @Test
    @DisplayName("Update user - Success")
    void updateUser_ValidId_ReturnsUpdatedUser() {
        Set<Role> newRoles = Set.of(Role.builder().id(UUID.randomUUID()).name("ADMIN").build());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleService.getRolesByIds(updateRequest.getRoleIds())).thenReturn(new HashSet<>(newRoles));
        when(passwordEncoder.encode(updateRequest.getPassword())).thenReturn("encodedNewPassword");
        when(userRepository.save(eq(user))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(userId, updateRequest);

        assertThat(result).isEqualTo(userDto);
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        assertThat(user.getRoles()).isEqualTo(newRoles);
        verify(userMapper).updateFromDto(
                argThat(dto -> dto.getLastName().equals("Updated")),
                eq(user)
        );
        verify(userRepository).save(eq(user));
    }

    @Test
    @DisplayName("Update non-existent user - Throws Exception")
    void updateUser_InvalidId_ThrowsException() {
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(eq(invalidId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(invalidId, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User does not exist");

        verify(userRepository).findById(eq(invalidId));
    }

    @Test
    @DisplayName("Delete user - Success")
    void deleteUser_ValidId_DeletesUser() {
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(eq(user));
    }

    @Test
    @DisplayName("Delete non-existent user - Throws Exception")
    void deleteUser_InvalidId_ThrowsException() {
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(eq(invalidId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(invalidId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User does not exist");
        verify(userRepository, times(1)).findById(eq(invalidId));
        verify(userRepository, never()).delete(any());
    }
}
