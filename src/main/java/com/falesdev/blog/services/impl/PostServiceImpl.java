package com.falesdev.blog.services.impl;

import com.falesdev.blog.domain.PostStatus;
import com.falesdev.blog.domain.dtos.PostDto;
import com.falesdev.blog.domain.dtos.requests.CreatePostRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdatePostRequestDto;
import com.falesdev.blog.domain.entities.Category;
import com.falesdev.blog.domain.entities.Post;
import com.falesdev.blog.domain.entities.Tag;
import com.falesdev.blog.domain.entities.User;
import com.falesdev.blog.mappers.PostMapper;
import com.falesdev.blog.respositories.CategoryRepository;
import com.falesdev.blog.respositories.PostRepository;
import com.falesdev.blog.respositories.UserRepository;
import com.falesdev.blog.services.PostService;
import com.falesdev.blog.services.TagService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagService tagService;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final CategoryRepository categoryRepository;

    private static final int WORDS_PER_MINUTE = 200;

    @Override
    public PostDto getPost(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post does not exist with ID " + id));
        return postMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDto> getAllPosts(UUID categoryId, UUID tagId) {
        if(categoryId != null && tagId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            Tag tag = tagService.getTagById(tagId);
            List<Post> posts = postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatus.PUBLISHED,
                    category,
                    tag
            );

            return posts.stream().map(postMapper::toDto).toList();
        }

        if(categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            List<Post> posts = postRepository.findAllByStatusAndCategory(
                    PostStatus.PUBLISHED,
                    category
            );

            return posts.stream().map(postMapper::toDto).toList();
        }

        if(tagId != null) {
            Tag tag = tagService.getTagById(tagId);
            List<Post> posts = postRepository.findAllByStatusAndTagsContaining(
                    PostStatus.PUBLISHED,
                    tag
            );

            return posts.stream().map(postMapper::toDto).toList();
        }

        List<Post> posts = postRepository.findAllByStatus(PostStatus.PUBLISHED);
        return posts.stream().map(postMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDto> getDraftPosts(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        List<Post> postAuthorDraft = postRepository.findAllByAuthorAndStatus(user, PostStatus.DRAFT);
        return postAuthorDraft.stream().map(postMapper::toDto).toList();
    }

    @Override
    @Transactional
    public PostDto createPost(UUID userId, CreatePostRequestDto createPostRequestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Post newPost = postMapper.toCreatePost(createPostRequestDto);

        newPost.setAuthor(user);
        newPost.setReadingTime(calculateReadingTime(createPostRequestDto.getContent()));

        Category category = categoryRepository.findById(createPostRequestDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        newPost.setCategory(category);

        Set<UUID> tagIds = createPostRequestDto.getTagIds();
        List<Tag> tags = tagService.getTagByIds(tagIds);
        newPost.setTags(new HashSet<>(tags));

        Post createdPost =  postRepository.save(newPost);

        return postMapper.toDto(createdPost);
    }

    @Override
    @Transactional
    public PostDto updatePost(UUID id, UpdatePostRequestDto updatePostRequestDto) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post does not exist with id " + id));


        postMapper.updateFromDto(updatePostRequestDto,existingPost);
        existingPost.setReadingTime(calculateReadingTime(updatePostRequestDto.getContent()));

        UUID updatePostRequestCategoryId = updatePostRequestDto.getCategoryId();
        if(!existingPost.getCategory().getId().equals(updatePostRequestCategoryId)) {

            Category newCategory = categoryRepository.findById(updatePostRequestCategoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            existingPost.setCategory(newCategory);
        }

        Set<UUID> existingTagIds = existingPost.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
        Set<UUID> updatePostRequestTagIds = updatePostRequestDto.getTagIds();
        if(!existingTagIds.equals(updatePostRequestTagIds)) {
            List<Tag> newTags = tagService.getTagByIds(updatePostRequestTagIds);
            existingPost.setTags(new HashSet<>(newTags));
        }

        Post updatedPost = postRepository.save(existingPost);

        return postMapper.toDto(updatedPost);
    }

    @Override
    public void deletePost(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post does not exist with ID " + id));
        postRepository.delete(post);
    }

    private Integer calculateReadingTime(String content) {
        if(content == null || content.isEmpty()) {
            return 0;
        }

        int wordCount = content.trim().split("\\s+").length;
        return (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE);
    }
}
