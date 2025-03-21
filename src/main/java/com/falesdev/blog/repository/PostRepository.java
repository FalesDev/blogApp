package com.falesdev.blog.repository;

import com.falesdev.blog.domain.PostStatus;
import com.falesdev.blog.domain.entity.Category;
import com.falesdev.blog.domain.entity.Post;
import com.falesdev.blog.domain.entity.Tag;
import com.falesdev.blog.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    // Solo tag
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE p.status = :status AND t = :tag")
    Page<Post> findAllByStatusAndTag(
            @Param("status") PostStatus status,
            @Param("tag") Tag tag,
            Pageable pageable
    );

    Page<Post> findAllByStatusAndCategory(
            PostStatus status,
            Category category,
            Pageable pageable
    );

    Page<Post> findAllByStatus(
            PostStatus status,
            Pageable pageable
    );

    Page<Post> findAllByAuthorAndStatus(
            User author,
            PostStatus status,
            Pageable pageable);

    Page<Post> findAllByTitleContainingIgnoreCaseAndStatus(
            String title,
            PostStatus status,
            Pageable pageable);
}
