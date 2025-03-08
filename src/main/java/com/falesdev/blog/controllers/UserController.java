package com.falesdev.blog.controllers;

import com.falesdev.blog.domain.dtos.UserDto;
import com.falesdev.blog.domain.dtos.requests.CreateUserRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdateUserRequestDto;
import com.falesdev.blog.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequestDto createUserRequestDto){
        return new ResponseEntity<>(userService.createUser(createUserRequestDto), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id, @Valid @RequestBody UpdateUserRequestDto updateUserRequestDto){
        return ResponseEntity.ok(userService.updateUser(id,updateUserRequestDto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
