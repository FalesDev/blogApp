package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.CategoryDto;
import com.falesdev.blog.domain.dto.request.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateCategoryRequestDto;
import com.falesdev.blog.domain.entity.Category;
import com.falesdev.blog.domain.entity.Post;
import com.falesdev.blog.mapper.CategoryMapper;
import com.falesdev.blog.repository.CategoryRepository;
import com.falesdev.blog.service.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDto categoryDto;
    private final UUID categoryId = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        category = Category.builder()
                .id(categoryId)
                .name("Programming")
                .posts(new ArrayList<>())
                .build();

        categoryDto = CategoryDto.builder()
                .id(categoryId)
                .name("Programming")
                .postCount(0)
                .build();
    }

    @Test
    @DisplayName("List all categories - Success")
    void listCategories_ReturnsCategoryDtoList() {
        List<Category> categories = List.of(category);
        when(categoryRepository.findAllWithPostCount()).thenReturn(categories);
        when(categoryMapper.toDto(eq(category))).thenReturn(categoryDto);

        List<CategoryDto> result = categoryService.listCategories();

        assertThat(result).containsExactly(categoryDto).hasSize(1);
        verify(categoryRepository).findAllWithPostCount();
        verify(categoryMapper).toDto(eq(category));
    }

    @Test
    @DisplayName("Get category by existing ID - Success")
    void getCategoryById_ValidId_ReturnsCategoryDto() {
        when(categoryRepository.findById(eq(categoryId))).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(eq(category))).thenReturn(categoryDto);

        CategoryDto result = categoryService.getCategoryById(categoryId);

        assertThat(result).isEqualTo(categoryDto);
        verify(categoryRepository).findById(eq(categoryId));
    }

    @Test
    @DisplayName("Get category by nonexistent ID - Exception")
    void getCategoryById_InvalidId_ThrowsEntityNotFoundException() {
        UUID nonExistingId = UUID.randomUUID();
        when(categoryRepository.findById(eq(nonExistingId))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(nonExistingId));

        verify(categoryRepository).findById(eq(nonExistingId));
    }

    @Test
    @DisplayName("Create new category - Success")
    void createCategory_NewCategory_ShouldReturnCreatedCategory() {
        UUID categoryId = UUID.randomUUID();
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("DevOps");
        Category newCategory = Category.builder().id(categoryId).name("DevOps").build();
        CategoryDto expectedDto = CategoryDto.builder().id(categoryId).name("DevOps").postCount(0).build();

        when(categoryRepository.existsByNameIgnoreCase(eq("DevOps"))).thenReturn(false);
        when(categoryMapper.toCreateCategory(eq(request))).thenReturn(newCategory);
        when(categoryRepository.save(eq(newCategory))).thenReturn(newCategory);
        when(categoryMapper.toDto(eq(newCategory))).thenReturn(expectedDto);

        CategoryDto result = categoryService.createCategory(request);

        assertThat(result.getName()).isEqualTo("DevOps");
        verify(categoryRepository).existsByNameIgnoreCase(eq("DevOps"));
        verify(categoryMapper).toCreateCategory(eq(request));
        verify(categoryRepository).save(eq(newCategory));
    }

    @Test
    @DisplayName("Create existing category - Exception")
    void createCategory_DuplicateName_ShouldThrowException() {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Programming");
        when(categoryRepository.existsByNameIgnoreCase(eq("Programming"))).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category already exists");

        verify(categoryRepository).existsByNameIgnoreCase(eq("Programming"));
    }

    @Test
    @DisplayName("Update existing category - Success")
    void updateCategory_ExistingCategory_ShouldReturnUpdatedCategory() {
        UUID existingCategoryId = UUID.randomUUID();
        Category existingCategory = Category.builder().id(existingCategoryId).name("Old Category").build();
        UpdateCategoryRequestDto request = new UpdateCategoryRequestDto(existingCategoryId, "New Category");
        CategoryDto expectedDto = CategoryDto.builder().id(existingCategoryId).name("New Category").build();

        when(categoryRepository.findById(eq(existingCategoryId))).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(eq(existingCategory))).thenReturn(existingCategory);
        when(categoryMapper.toDto(eq(existingCategory))).thenReturn(expectedDto);

        CategoryDto result = categoryService.updateCategory(existingCategoryId, request);

        assertThat(result.getName()).isEqualTo("New Category");
        verify(categoryMapper).updateFromDto(
                argThat(dto -> dto.getName().equals("New Category")),
                eq(existingCategory)
        );
        verify(categoryRepository).save(eq(existingCategory));
    }

    @Test
    @DisplayName("Delete category without posts - Success")
    void deleteCategory_WithoutPosts_ShouldDelete() {
        when(categoryRepository.findById(eq(categoryId))).thenReturn(Optional.of(category));

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository, times(1)).deleteById(eq(categoryId));
    }
    @Test
    @DisplayName("Delete category with posts - Exception")
    void deleteCategory_WithPosts_ShouldThrowException() {
        category.getPosts().add(Post.builder().build());
        when(categoryRepository.findById(eq(categoryId))).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("posts associated");

        verify(categoryRepository).findById(eq(categoryId));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete nonexistent category - Exception")
    void deleteCategory_NonexistentCategory_ShouldThrowException() {
        UUID nonExistingId = UUID.randomUUID();
        when(categoryRepository.findById(eq(nonExistingId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(nonExistingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository).findById(eq(nonExistingId));
        verify(categoryRepository, never()).delete(any());
    }
}
