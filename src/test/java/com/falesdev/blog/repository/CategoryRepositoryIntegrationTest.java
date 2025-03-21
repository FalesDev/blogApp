package com.falesdev.blog.repository;
import com.falesdev.blog.domain.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CategoryRepositoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .name("Testing")
                .build();

        entityManager.persistAndFlush(category);
    }

    @Test
    void saveCategory() {
        Category category = Category.builder()
                .name("BigData")
                .build();

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("BigData");
    }

    @Test
    void findById_CategoryExists() {
        Optional<Category> foundCategory = categoryRepository.findById(category.getId());

        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Testing");
    }

    @Test
    void findById_CategoryNotExists() {
        UUID uuid = UUID.randomUUID();
        Optional<Category> foundCategory = categoryRepository.findById(uuid);

        assertThat(foundCategory).isEmpty();
    }

    @Test
    void deleteCategory() {
        UUID categoryId = category.getId();
        assertThat(categoryRepository.existsById(categoryId)).isTrue();

        categoryRepository.delete(category);
        entityManager.flush();

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    void existsByNameIgnoreCase() {
        boolean exists1 = categoryRepository.existsByNameIgnoreCase("TESTING");
        boolean exists2 = categoryRepository.existsByNameIgnoreCase("testing");
        boolean exists3 = categoryRepository.existsByNameIgnoreCase("Science");

        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
        assertThat(exists3).isFalse();
    }
}
