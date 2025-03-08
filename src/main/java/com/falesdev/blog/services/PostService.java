package com.falesdev.blog.services;

import com.falesdev.blog.domain.dtos.PostDto;
import com.falesdev.blog.domain.dtos.requests.CreatePostRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdatePostRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.UUID;

public interface PostService {

    PostDto getPost(UUID id);
    Page<PostDto> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable);
    Page<PostDto> getDraftPosts(UUID userId, Pageable pageable);
    PostDto createPost(UUID id, CreatePostRequestDto createPostRequestDto);
    PostDto updatePost(UUID id, UpdatePostRequestDto updatePostRequestDto);
    void deletePost(UUID id);
}
