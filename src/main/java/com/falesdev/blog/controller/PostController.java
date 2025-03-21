package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.PostDto;
import com.falesdev.blog.domain.dto.request.CreatePostRequestDto;
import com.falesdev.blog.domain.dto.request.UpdatePostRequestDto;
import com.falesdev.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/search")
    public ResponseEntity<Page<PostDto>> getAllPostsbyTitle(
            @RequestParam(required = false) String title,
            Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getAllPostsbyTitle(title, pageable));
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
