package com.falesdev.blog.repository;

import com.falesdev.blog.domain.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class TagRepositoryIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        tag1 = Tag.builder()
                .name("Development")
                .build();

        tag2 = Tag.builder()
                .name("Git")
                .build();

        entityManager.persistAndFlush(tag1);
        entityManager.persistAndFlush(tag2);
    }

    @Test
    void saveTag() {
        Tag tag = Tag.builder()
                .name("Testing")
                .build();

        Tag savedTag = tagRepository.save(tag);

        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getName()).isEqualTo("Testing");
    }

    @Test
    void findById_TagExists() {
        Optional<Tag> foundTag = tagRepository.findById(tag1.getId());

        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getName()).isEqualTo("Development");
    }

    @Test
    void findById_TagNotExists() {
        UUID uuid = UUID.randomUUID();
        Optional<Tag> foundTag = tagRepository.findById(uuid);

        assertThat(foundTag).isEmpty();
    }

    @Test
    void deleteTag() {
        UUID tag1Id = tag1.getId();
        assertThat(tagRepository.existsById(tag1Id)).isTrue();

        tagRepository.delete(tag1);
        entityManager.flush();

        assertThat(tagRepository.findById(tag1Id)).isEmpty();
    }

    @Test
    void findByNameIn() {
        Set<String> existingNames = Set.of("Development", "Git");
        Set<String> nonExistingNames = Set.of("Python", "Angular");
        Set<String> mixedNames = Set.of("Development", "Python");

        List<Tag> foundTags = tagRepository.findByNameIn(existingNames);
        List<Tag> emptyResult = tagRepository.findByNameIn(nonExistingNames);
        List<Tag> mixedResult = tagRepository.findByNameIn(mixedNames);

        assertThat(foundTags)
                .hasSize(2)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("Development", "Git");

        assertThat(emptyResult).isEmpty();
        assertThat(mixedResult)
                .hasSize(1)
                .extracting(Tag::getName)
                .containsExactly("Development");
    }
}
