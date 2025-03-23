package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.PostDto;
import com.falesdev.blog.domain.dto.request.CreatePostRequestDto;
import com.falesdev.blog.domain.dto.request.UpdatePostRequestDto;
import com.falesdev.blog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Post", description = "Controller for Posts")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "Get all posts",
            description = "Returns paginated list of posts with optional category/tag filtering"
    )
    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPosts(categoryId, tagId,pageable));
    }

    @Operation(
            summary = "Get post by ID",
            description = "Returns a single post with specified identifier"
    )
    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDto> getPost(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @Operation(
            summary = "Get user drafts",
            description = "Returns paginated list of draft posts for current user"
    )
    @GetMapping(path = "/drafts")
    public ResponseEntity<Page<PostDto>> getDrafts(
            @RequestAttribute UUID userId,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getDraftPosts(userId,pageable));
    }

    @Operation(
            summary = "Search posts by title",
            description = "Returns paginated list of posts filtered by title"
    )
    @GetMapping("/search")
    public ResponseEntity<Page<PostDto>> getAllPostsbyTitle(
            @RequestParam(required = false) String title,
            Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getAllPostsbyTitle(title, pageable));
    }

    @Operation(
            summary = "Create new post",
            description = "Creates a new post and returns the created entity"
    )
    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody CreatePostRequestDto createPostRequestDto,
            @RequestAttribute UUID userId) {
        return new ResponseEntity<>(postService.createPost(userId, createPostRequestDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update post",
            description = "Updates an existing post by its ID"
    )
    @PutMapping(path = "/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequestDto updatePostRequestDto) {
        return ResponseEntity.ok(postService.updatePost(id, updatePostRequestDto));
    }

    @Operation(
            summary = "Delete post by ID",
            description = "Delete a post by its identifier"
    )
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
