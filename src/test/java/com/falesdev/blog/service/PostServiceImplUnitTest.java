package com.falesdev.blog.service;

import com.falesdev.blog.domain.PostStatus;
import com.falesdev.blog.domain.dto.PostDto;
import com.falesdev.blog.domain.dto.request.CreatePostRequestDto;
import com.falesdev.blog.domain.dto.request.UpdatePostRequestDto;
import com.falesdev.blog.domain.entity.Category;
import com.falesdev.blog.domain.entity.Post;
import com.falesdev.blog.domain.entity.Tag;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.mapper.PostMapper;
import com.falesdev.blog.repository.CategoryRepository;
import com.falesdev.blog.repository.PostRepository;
import com.falesdev.blog.repository.UserRepository;
import com.falesdev.blog.service.impl.PostServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplUnitTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagService tagService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private Post post;
    private PostDto postDto;
    private User author;
    private Category category;
    private Tag tag;
    private final UUID postId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID tagId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        author = User.builder().id(userId).build();
        category = Category.builder().id(categoryId).build();
        tag = Tag.builder().id(tagId).build();

        post = Post.builder()
                .id(postId)
                .title("Test Post")
                .content("Test content")
                .status(PostStatus.PUBLISHED)
                .author(author)
                .category(category)
                .tags(Set.of(tag))
                .readingTime(5)
                .createdAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();

        postDto = PostDto.builder()
                .id(postId)
                .title("Test Post")
                .content("Test content")
                .readingTime(5)
                .status(PostStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Get existing post - Success")
    void getPost_ExistingId_ReturnsPostDto() {
        when(postRepository.findById(eq(postId))).thenReturn(Optional.of(post));
        when(postMapper.toDto(post)).thenReturn(postDto);

        PostDto result = postService.getPost(postId);

        assertThat(result).isEqualTo(postDto);
        verify(postRepository).findById(eq(postId));
    }

    @Test
    @DisplayName("Get post by invalid ID - Throws Exception")
    void getPost_InvalidId_ThrowsException() {
        UUID invalidId = UUID.randomUUID();
        when(postRepository.findById(eq(invalidId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(invalidId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Post does not exist");
        verify(postRepository).findById(eq(invalidId));
    }

    @Test
    @DisplayName("Get all posts by category - Success")
    void getAllPosts_WithCategory_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post));

        when(categoryRepository.findById(eq(categoryId))).thenReturn(Optional.of(category));
        when(postRepository.findAllByStatusAndCategory(eq(PostStatus.PUBLISHED), eq(category), eq(pageable)))
                .thenReturn(postPage);
        when(postMapper.toDto(eq(post))).thenReturn(postDto);

        Page<PostDto> result = postService.getAllPosts(categoryId, null, pageable);

        assertThat(result.getContent()).containsExactlyInAnyOrder(postDto);
        verify(categoryRepository).findById(eq(categoryId));
        verify(postRepository).findAllByStatusAndCategory(eq(PostStatus.PUBLISHED), eq(category), eq(pageable));
        verify(postMapper).toDto(eq(post));
    }

    @Test
    @DisplayName("Get draft posts for user - Success")
    void getDraftPosts_ValidUser_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post));

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(author));
        when(postRepository.findAllByAuthorAndStatus(eq(author), eq(PostStatus.DRAFT), eq(pageable)))
                .thenReturn(postPage);
        when(postMapper.toDto(eq(post))).thenReturn(postDto);

        Page<PostDto> result = postService.getDraftPosts(userId, pageable);

        assertThat(result.getContent()).containsExactly(postDto);
        verify(userRepository).findById(eq(userId));
        verify(postRepository).findAllByAuthorAndStatus(eq(author), eq(PostStatus.DRAFT), eq(pageable));
        verify(postMapper).toDto(eq(post));
    }

    @Test
    @DisplayName("Search posts by title - Success")
    void getAllPostsByTitle_ValidTitle_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post));
        String title = "Test";

        when(postRepository
                .findAllByTitleContainingIgnoreCaseAndStatus(eq(title), eq(PostStatus.PUBLISHED), eq(pageable)))
                .thenReturn(postPage);
        when(postMapper.toDto(eq(post))).thenReturn(postDto);

        Page<PostDto> result = postService.getAllPostsbyTitle(title, pageable);

        assertThat(result.getContent()).containsExactly(postDto);
        verify(postRepository)
                .findAllByTitleContainingIgnoreCaseAndStatus(eq(title), eq(PostStatus.PUBLISHED), eq(pageable));
        verify(postMapper).toDto(eq(post));
    }

    @Test
    @DisplayName("Create post - Success")
    void createPost_ValidRequest_ReturnsPostDto() {
        CreatePostRequestDto request = new CreatePostRequestDto(
                "New Post",
                "Content",
                categoryId,
                Set.of(tagId),
                PostStatus.DRAFT
        );

        Post newPost = Post.builder().build();
        Post savedPost = Post.builder().id(postId).build();

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(author));
        when(categoryRepository.findById(eq(categoryId))).thenReturn(Optional.of(category));
        when(tagService.getTagByIds(eq(Set.of(tagId)))).thenReturn(List.of(tag));
        when(postMapper.toCreatePost(request)).thenReturn(newPost);
        when(postRepository.save(eq(newPost))).thenReturn(savedPost);
        when(postMapper.toDto(savedPost)).thenReturn(postDto);

        PostDto result = postService.createPost(userId, request);

        assertThat(result).isEqualTo(postDto);
        assertThat(newPost.getAuthor()).isEqualTo(author);
        assertThat(newPost.getCategory()).isEqualTo(category);
        assertThat(newPost.getTags()).containsExactly(tag);
        verify(userRepository).findById(eq(userId));
        verify(categoryRepository).findById(eq(categoryId));
        verify(tagService).getTagByIds(eq(Set.of(tagId)));
        verify(postMapper).toCreatePost(eq(request));
        verify(postRepository).save(eq(newPost));
        verify(postMapper).toDto(eq(savedPost));
    }

    @Test
    @DisplayName("Update post - Change category and tags")
    void updatePost_ChangeCategoryAndTags_ReturnsUpdatedPost() {
        UpdatePostRequestDto request = new UpdatePostRequestDto(
                UUID.randomUUID(),
                "Updated Title",
                "Updated content",
                UUID.randomUUID(),
                Set.of(UUID.randomUUID()),
                PostStatus.PUBLISHED
        );

        Category newCategory = Category.builder().id(request.getCategoryId()).build();
        Tag newTag = Tag.builder().id(request.getTagIds().iterator().next()).build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.of(newCategory));
        when(tagService.getTagByIds(request.getTagIds())).thenReturn(List.of(newTag));
        when(postRepository.save(eq(post))).thenReturn(post);
        when(postMapper.toDto(post)).thenReturn(postDto);

        PostDto result = postService.updatePost(postId, request);

        assertThat(result).isEqualTo(postDto);
        assertThat(post.getCategory()).isEqualTo(newCategory);
        assertThat(post.getTags()).containsExactly(newTag);
        verify(postMapper).updateFromDto(eq(request), eq(post));
        verify(postMapper).updateFromDto(
                argThat(dto -> dto.getTitle().equals("Updated Title")),
                eq(post)
        );
        verify(postRepository).save(eq(post));
    }

    @Test
    @DisplayName("Update non-existent post - Throws Exception")
    void updatePost_InvalidId_ThrowsException() {
        UUID invalidId = UUID.randomUUID();
        when(postRepository.findById(eq(invalidId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(invalidId, any(UpdatePostRequestDto.class)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Post does not exist");

        verify(postRepository).findById(eq(invalidId));
    }

    @Test
    @DisplayName("Delete existing post - Success")
    void deletePost_ValidId_DeletesPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(postId);

        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("Delete non-existent post - Throws Exception")
    void deletePost_InvalidId_ThrowsException() {
        UUID invalidId = UUID.randomUUID();
        when(postRepository.findById(eq(invalidId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(invalidId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Post does not exist");
        verify(postRepository, times(1)).findById(eq(invalidId));
        verify(postRepository, never()).delete(any());
    }
}
