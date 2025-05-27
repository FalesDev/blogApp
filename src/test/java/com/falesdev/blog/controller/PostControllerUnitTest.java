package com.falesdev.blog.controller;

import com.falesdev.blog.domain.PostStatus;
import com.falesdev.blog.domain.dto.*;
import com.falesdev.blog.domain.dto.request.CreatePostRequestDto;
import com.falesdev.blog.domain.dto.request.UpdatePostRequestDto;
import com.falesdev.blog.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PostControllerUnitTest {

    @InjectMocks
    private PostController postController;

    @Mock
    private PostService postService;

    private static final Pageable pageable = PageRequest.of(0, 10,
            Sort.by("createdAt").descending());
    private PostDto postDto1, postDto2,expectedCreatedPostDto,expectedUpdatedPostDto;
    private CreatePostRequestDto createPostRequestDto;
    private UpdatePostRequestDto updatePostRequestDto;

    @BeforeEach
    public void setUp() {
        // Initialize unique IDs and variables
        AuthorDto author = AuthorDto.builder()
                .id(UUID.randomUUID())
                .firstName("Test Author")
                .lastName("Cyber")
                .build();
        CategoryDto category = CategoryDto.builder().id(UUID.randomUUID()).name("Test Category").postCount(2).build();
        TagDto tag = TagDto.builder().id(UUID.randomUUID()).name("Test Tag").postCount(5).build();

        // DTO to list/get
        postDto1 = createPostDto(
                "Spring Data JPA Guide",
                "Content...",
                author,
                category,
                Set.of(tag),
                PostStatus.PUBLISHED
        );
        postDto2 = createPostDto(
                "Spring Security Beginner Guide",
                "Content...",
                author,
                category,
                Set.of(tag),
                PostStatus.DRAFT
        );

        // DTOs for the creation test
        createPostRequestDto = new CreatePostRequestDto(
                "Angular Beginner Guide",
                "Content...",
                category.getId(),
                Set.of(tag.getId()),
                PostStatus.PUBLISHED
        );
        expectedCreatedPostDto = createPostDto(
                "Angular Beginner Guide",
                "Content...",
                author,
                category,
                Set.of(tag),
                PostStatus.PUBLISHED
        );

        // DTO for update
        updatePostRequestDto = new UpdatePostRequestDto(
                UUID.randomUUID(),
                "React Beginner Guide",
                "Content...",
                category.getId(),
                Set.of(tag.getId()),
                PostStatus.PUBLISHED);
        expectedUpdatedPostDto = createPostDto(
                "React Beginner Guide",
                "Content...",
                author,
                category,
                Set.of(tag),
                PostStatus.PUBLISHED
        );
    }

    @Test
    @DisplayName("Success Get Posts - No Filters")
    void getAllPosts_NoFilters_ReturnsPage() {
        Page<PostDto> expectedPage = new PageImpl<>(List.of(postDto1,postDto2));
        when(postService.getAllPosts(isNull(), isNull(), eq(pageable))).thenReturn(expectedPage);

        ResponseEntity<Page<PostDto>> response = postController.getAllPosts(null, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPage).hasSize(2);

        verify(postService).getAllPosts(isNull(), isNull(), eq(pageable));
    }

    @Test
    @DisplayName("Success Get Posts - With Filter")
    void getAllPosts_WithFilters_ReturnsPage() {
        UUID categoryId = UUID.randomUUID();
        Page<PostDto> expectedPage = pageOf(postDto1);

        when(postService.getAllPosts(eq(categoryId), isNull(), eq(pageable))).thenReturn(expectedPage);

        ResponseEntity<Page<PostDto>> response = postController.getAllPosts(categoryId, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPage).hasSize(1);

        verify(postService).getAllPosts(eq(categoryId), isNull(), eq(pageable));
    }

    @Test
    @DisplayName("Success Get Post")
    void getPost_ShouldReturnPost_WhenPostExists() {
        UUID postId = UUID.randomUUID();
        when(postService.getPost(eq(postId))).thenReturn(postDto1);

        ResponseEntity<PostDto> response = postController.getPost(postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(postDto1);

        verify(postService).getPost(eq(postId));
    }

    @Test
    @DisplayName("Fail to Get Post - Not Found")
    void getPost_ShouldThrowException_WhenPostNotFound() {
        UUID postId = UUID.randomUUID();
        when(postService.getPost(eq(postId))).thenThrow(new EntityNotFoundException("Post not found"));

        Exception exception = assertThrows(EntityNotFoundException.class, () -> postController.getPost(postId));
        assertThat(exception.getMessage()).isEqualTo("Post not found");

        verify(postService).getPost(eq(postId));
    }

    @Test
    @DisplayName("Success Get Drafts by UserId")
    void getDrafts_ValidUserId_ReturnsPage() {
        UUID userId = UUID.randomUUID();
        Page<PostDto> expectedPage = pageOf(postDto2);
        when(postService.getDraftPosts(eq(userId), eq(pageable))).thenReturn(expectedPage);

        ResponseEntity<Page<PostDto>> response = postController.getDrafts(userId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPage).hasSize(1);

        verify(postService).getDraftPosts(eq(userId), eq(pageable));
    }

    @Test
    @DisplayName("Success Get Posts by Title")
    void searchPosts_WithTitle_ReturnsPage() {
        String title = "Guide";
        Page<PostDto> expectedPage = pageOf(postDto1,postDto2);

        when(postService.getAllPostsbyTitle(eq(title), eq(pageable))).thenReturn(expectedPage);

        ResponseEntity<Page<PostDto>> response = postController.getAllPostsbyTitle(title, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPage).hasSize(2);

        verify(postService).getAllPostsbyTitle(eq(title), eq(pageable));
    }

    @Test
    @DisplayName("Success Create Post")
    void createPost_ShouldReturnCreatedPost() {
        UUID userId = UUID.randomUUID();
        when(postService.createPost(eq(userId),eq(createPostRequestDto)))
                .thenReturn(expectedCreatedPostDto);

        ResponseEntity<PostDto> response = postController.createPost(createPostRequestDto,userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedCreatedPostDto);

        verify(postService).createPost(eq(userId),eq(createPostRequestDto));
    }

    @Test
    @DisplayName("Success Update Post")
    void updatePost_ShouldReturnUpdatedPost() {
        UUID updatePostId = UUID.randomUUID();
        when(postService.updatePost(eq(updatePostId), eq(updatePostRequestDto)))
                .thenReturn(expectedUpdatedPostDto);

        ResponseEntity<PostDto> response = postController.updatePost(updatePostId, updatePostRequestDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedUpdatedPostDto);

        verify(postService).updatePost(eq(updatePostId), eq(updatePostRequestDto));
    }

    @Test
    @DisplayName("Success Delete Post")
    void deletePost_ShouldReturnNoContent() {
        UUID postId = UUID.randomUUID();
        doNothing().when(postService).deletePost(eq(postId));

        ResponseEntity<Void> response = postController.deletePost(postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(postService, times(1)).deletePost(eq(postId));
    }

    private PostDto createPostDto(String title, String content, AuthorDto author,
                                  CategoryDto category, Set<TagDto> tags, PostStatus status) {
        return PostDto.builder()
                .id(UUID.randomUUID())
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .tags(tags)
                .readingTime(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(status)
                .build();
    }

    private Page<PostDto> pageOf(PostDto... posts) {
        return new PageImpl<>(List.of(posts));
    }
}
