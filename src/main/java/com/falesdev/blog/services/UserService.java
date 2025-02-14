package com.falesdev.blog.services;

import com.falesdev.blog.domain.entities.User;

import java.util.UUID;

public interface UserService {

    User getUserById(UUID id);
}
