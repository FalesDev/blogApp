package com.falesdev.blog.services;

import com.falesdev.blog.domain.dtos.PostDto;
import com.falesdev.blog.domain.dtos.requests.CreatePostRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdatePostRequestDto;


import java.util.List;
import java.util.UUID;

public interface PostService {

    PostDto getPost(UUID id);
    List<PostDto> getAllPosts(UUID categoryId, UUID tagId);
    List<PostDto> getDraftPosts(UUID userId);
    PostDto createPost(UUID id, CreatePostRequestDto createPostRequestDto);
    PostDto updatePost(UUID id, UpdatePostRequestDto updatePostRequestDto);
    void deletePost(UUID id);
}
