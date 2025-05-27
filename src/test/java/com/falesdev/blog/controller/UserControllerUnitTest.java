package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.*;
import com.falesdev.blog.domain.dto.request.CreateUserRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateUserRequestDto;
import com.falesdev.blog.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private UserDto userDto,expectedCreatedUserDto,expectedUpdatedUserDto;
    private CreateUserRequestDto createUserRequestDto;
    private UpdateUserRequestDto updateUserRequestDto;

    @BeforeEach
    void setUp() {
        // Initialize unique IDs and variables
        RoleDto role = RoleDto.builder().id(UUID.randomUUID()).name("ADMIN").build();

        // DTO to list/get
        userDto = createUserDto(
                "fabricio-1998-xd@hotmail.com",
                "Fabricio",
                "Rodriguez",
                Set.of(role)
        );

        // DTOs for the creation test
        createUserRequestDto = CreateUserRequestDto.builder()
                .email("renato-1998-xd@hotmail.com")
                .firstName("Renato")
                .lastName("Quiñones")
                .roleIds(Set.of(role.getId()))
                .build();
        expectedCreatedUserDto = createUserDto(
                "renato-1998-xd@hotmail.com",
                "Renato",
                "Quiñones",
                Set.of(role)
        );

        // DTO for update
        updateUserRequestDto = UpdateUserRequestDto.builder()
                .id(UUID.randomUUID())
                .email("mario-1998-xd@hotmail.com")
                .firstName("Mario")
                .lastName("Gutierrez")
                .roleIds(Set.of(role.getId()))
                .build();
        expectedUpdatedUserDto = createUserDto(
                "mario-1998-xd@hotmail.com",
                "Mario",
                "Gutierrez",
                Set.of(role)
        );
    }

    @Test
    @DisplayName("Success Get Users")
    void listUsers_ShouldReturnListOfUsers() {
        List<UserDto> userList = List.of(userDto);
        when(userService.getAllUsers()).thenReturn(userList);

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userList).hasSize(1);

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Success Get User")
    void getUser_ShouldReturnUser_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(eq(userId))).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUser(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(userDto);

        verify(userService).getUserById(eq(userId));
    }

    @Test
    @DisplayName("Fail to Get User - Not Found")
    void getUser_ShouldThrowException_WhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(eq(userId))).thenThrow(new EntityNotFoundException("User not found"));

        Exception exception = assertThrows(EntityNotFoundException.class, () -> userController.getUser(userId));
        assertThat(exception.getMessage()).isEqualTo("User not found");

        verify(userService).getUserById(eq(userId));
    }

    @Test
    @DisplayName("Success Create User")
    void createUser_ShouldReturnCreatedUser() {
        when(userService.createUser(eq(createUserRequestDto)))
                .thenReturn(expectedCreatedUserDto);

        ResponseEntity<UserDto> response = userController.createUser(createUserRequestDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedCreatedUserDto);

        verify(userService).createUser(eq(createUserRequestDto));
    }

    @Test
    @DisplayName("Success Update User")
    void updateUser_ShouldReturnUpdatedUser() {
        UUID updateUserId = UUID.randomUUID();
        when(userService.updateUser(eq(updateUserId), eq(updateUserRequestDto)))
                .thenReturn(expectedUpdatedUserDto);

        ResponseEntity<UserDto> response = userController.updateUser(updateUserId, updateUserRequestDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedUpdatedUserDto);

        verify(userService).updateUser(eq(updateUserId), eq(updateUserRequestDto));
    }

    @Test
    @DisplayName("Success Delete User")
    void deleteUser_ShouldReturnNoContent() {
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).deleteUser(eq(userId));

        ResponseEntity<Void> response = userController.deleteUser(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(userService, times(1)).deleteUser(eq(userId));
    }

    private UserDto createUserDto(String email, String firstName, String lastName,
                                  Set<RoleDto> roles) {
        return UserDto.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password("securepass")
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
