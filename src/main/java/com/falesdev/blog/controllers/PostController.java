package com.falesdev.blog.controllers;

import com.falesdev.blog.domain.dtos.PostDto;
import com.falesdev.blog.domain.dtos.requests.CreatePostRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdatePostRequestDto;
import com.falesdev.blog.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPosts(categoryId, tagId,pageable));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDto> getPost(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @GetMapping(path = "/drafts")
    public ResponseEntity<Page<PostDto>> getDrafts(
            @RequestAttribute UUID userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getDraftPosts(userId,pageable));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody CreatePostRequestDto createPostRequestDto,
            @RequestAttribute UUID userId) {
        return new ResponseEntity<>(postService.createPost(userId, createPostRequestDto), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequestDto updatePostRequestDto) {
        return ResponseEntity.ok(postService.updatePost(id, updatePostRequestDto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
