package com.falesdev.blog.service.impl;

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
import com.falesdev.blog.service.PostService;
import com.falesdev.blog.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Transactional(readOnly = true)
    public PostDto getPost(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post does not exist"));
        return postMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable) {
        if(categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            return postRepository.findAllByStatusAndCategory(
                    PostStatus.PUBLISHED,
                    category,
                    pageable
            ).map(postMapper::toDto);
        }

        if(tagId != null) {
            Tag tag = tagService.getTagById(tagId);
            return postRepository.findAllByStatusAndTag(
                    PostStatus.PUBLISHED,
                    tag,
                    pageable
            ).map(postMapper::toDto);
        }

        return postRepository.findAllByStatus(PostStatus.PUBLISHED, pageable)
                .map(postMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getDraftPosts(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return postRepository.findAllByAuthorAndStatus(
                user,
                PostStatus.DRAFT,
                pageable
        ).map(postMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPostsbyTitle(String title, Pageable pageable) {
        return postRepository.findAllByTitleContainingIgnoreCaseAndStatus(title, PostStatus.PUBLISHED, pageable)
                .map(postMapper::toDto);
    }

    @Override
    @Transactional
    public PostDto createPost(UUID userId, CreatePostRequestDto createPostRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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
                .orElseThrow(() -> new EntityNotFoundException("Post does not exist"));

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
                .orElseThrow(() -> new EntityNotFoundException("Post does not exist"));
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
