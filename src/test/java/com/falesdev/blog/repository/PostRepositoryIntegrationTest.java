package com.falesdev.blog.repository;

import com.falesdev.blog.domain.PostStatus;
import com.falesdev.blog.domain.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostRepositoryIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User author;
    private Category category1,category2;
    private Tag tag1,tag2;
    private Post post1,post2,post3;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().name("ADMIN").build();
        entityManager.persistAndFlush(role);

        author = User.builder()
                .email("fabricio@example.com")
                .password("securepass")
                .name("Fabricio Rodriguez Avalos")
                .roles(new HashSet<>(Set.of(role)))
                .build();
        entityManager.persistAndFlush(author);

        category1 = Category.builder().name("Backend").build();
        category2 = Category.builder().name("Frontend").build();

        entityManager.persistAndFlush(category1);
        entityManager.persistAndFlush(category2);

        tag1 = Tag.builder().name("Spring Boot").build();
        tag2 = Tag.builder().name("Java").build();

        entityManager.persistAndFlush(tag1);
        entityManager.persistAndFlush(tag2);

        post1 = Post.builder()
                .title("Spring Data JPA Guide")
                .content("Complete guide to Spring Data JPA...")
                .status(PostStatus.PUBLISHED)
                .readingTime(5)
                .author(author)
                .category(category1)
                .tags(Set.of(tag1))
                .build();
        entityManager.persistAndFlush(post1);

        post2 = Post.builder()
                .title("Angular Beginner Guide")
                .content("Getting started with Angular...")
                .status(PostStatus.PUBLISHED)
                .readingTime(10)
                .author(author)
                .category(category2)
                .tags(Set.of(tag2))
                .build();
        entityManager.persistAndFlush(post2);

        post3 = Post.builder()
                .title("Spring Security Beginner Guide")
                .content("Getting started with Spring Security...")
                .status(PostStatus.DRAFT)
                .readingTime(10)
                .author(author)
                .category(category1)
                .tags(Set.of(tag1))
                .build();
        entityManager.persistAndFlush(post3);
    }

    @Test
    void savePost() {
        Role role = Role.builder().name("USER").build();
        entityManager.persistAndFlush(role);

        User author = User.builder()
                .email("fabricio-1998-xd@hotmail.com")
                .password("securepass")
                .name("Fabricio Rodriguez")
                .roles(new HashSet<>(Set.of(role)))
                .build();
        entityManager.persistAndFlush(author);

        Category category = Category.builder().name("Web Development").build();
        entityManager.persistAndFlush(category);

        Tag tag = Tag.builder().name("Angular").build();
        entityManager.persistAndFlush(tag);

        post1 = Post.builder()
                .title("Angular Guide")
                .content("Complete guide to Angular...")
                .status(PostStatus.PUBLISHED)
                .readingTime(15)
                .author(author)
                .category(category)
                .tags(new HashSet<>(Set.of(tag)))
                .build();

        Post savedPost = postRepository.save(post1);

        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("Angular Guide");
        assertThat(savedPost.getContent()).isEqualTo("Complete guide to Angular...");
        assertThat(savedPost.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(savedPost.getReadingTime()).isEqualTo(15);
        assertThat(savedPost.getAuthor().getEmail()).isEqualTo("fabricio-1998-xd@hotmail.com");
        assertThat(savedPost.getCategory().getName()).isEqualTo("Web Development");
        assertThat(savedPost.getTags()).hasSize(1);
    }

    @Test
    void findById_PostExists() {
        Optional<Post> foundPost = postRepository.findById(post1.getId());

        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("Spring Data JPA Guide");
        assertThat(foundPost.get().getContent()).isEqualTo("Complete guide to Spring Data JPA...");
        assertThat(foundPost.get().getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(foundPost.get().getReadingTime()).isEqualTo(5);
        assertThat(foundPost.get().getAuthor().getEmail()).isEqualTo("fabricio@example.com");
        assertThat(foundPost.get().getCategory().getName()).isEqualTo("Backend");
        assertThat(foundPost.get().getTags()).hasSize(1);
    }

    @Test
    void findById_PostNotExists() {
        UUID uuid = UUID.randomUUID();
        Optional<Post> foundPost = postRepository.findById(uuid);

        assertThat(foundPost).isEmpty();
    }

    @Test
    void deletePost() {
        UUID postId = post1.getId();
        UUID categoryId = post1.getCategory().getId();
        UUID tagId = post1.getTags().iterator().next().getId();

        postRepository.deleteById(postId);
        entityManager.flush();

        assertThat(postRepository.findById(postId)).isEmpty();

        assertThat(entityManager.find(Category.class, categoryId)).isNotNull();
        assertThat(entityManager.find(Tag.class, tagId)).isNotNull();
    }

    @Test
    void findAllByStatusAndTag() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findAllByStatusAndTag(
                PostStatus.PUBLISHED,
                tag2,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Post::getTitle)
                .containsExactly("Angular Beginner Guide");
    }

    @Test
    void findAllByStatusAndCategory() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findAllByStatusAndCategory(
                PostStatus.PUBLISHED,
                category1,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Post::getTitle)
                .containsExactly("Spring Data JPA Guide");
    }

    @Test
    void findAllByStatus() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findAllByStatus(
                PostStatus.PUBLISHED,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(2);
    }

    @Test
    void findAllByAuthorAndStatus() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findAllByAuthorAndStatus(
                author,
                PostStatus.DRAFT,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Post::getTitle)
                .containsExactly("Spring Security Beginner Guide");
    }

    @Test
    void findAllByTitleContainingIgnoreCase() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findAllByTitleContainingIgnoreCaseAndStatus(
                "GUIDE",
                PostStatus.PUBLISHED,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Post::getTitle)
                .containsExactlyInAnyOrder("Spring Data JPA Guide","Angular Beginner Guide");
    }
}
