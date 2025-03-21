package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.PostDto;
import com.falesdev.blog.domain.dto.request.CreatePostRequestDto;
import com.falesdev.blog.domain.dto.request.UpdatePostRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

public interface PostService {

    PostDto getPost(UUID id);
    Page<PostDto> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable);
    Page<PostDto> getDraftPosts(UUID userId, Pageable pageable);
    Page<PostDto> getAllPostsbyTitle(String title, Pageable pageable);
    PostDto createPost(UUID id, CreatePostRequestDto createPostRequestDto);
    PostDto updatePost(UUID id, UpdatePostRequestDto updatePostRequestDto);
    void deletePost(UUID id);
}
