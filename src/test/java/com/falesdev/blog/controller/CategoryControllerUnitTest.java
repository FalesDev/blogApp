package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.CategoryDto;
import com.falesdev.blog.domain.dto.request.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateCategoryRequestDto;
import com.falesdev.blog.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CategoryControllerUnitTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    private CategoryDto categoryDto,expectedCreatedCategoryDto,expectedUpdatedCategoryDto;
    private CreateCategoryRequestDto createCategoryRequestDto;
    private UpdateCategoryRequestDto updateCategoryRequestDto;

    @BeforeEach
    void setUp() {
        // DTO to list/get
        categoryDto = CategoryDto.builder().id(UUID.randomUUID()).name("Programming").postCount(2).build();

        // DTOs for the creation test
        createCategoryRequestDto = CreateCategoryRequestDto.builder().name("Tech").build();
        expectedCreatedCategoryDto = CategoryDto.builder()
                .id(UUID.randomUUID())
                .name("Tech")
                .postCount(0)
                .build();

        // DTO for update
        updateCategoryRequestDto = UpdateCategoryRequestDto.builder().id(UUID.randomUUID()).name("Health").build();
        expectedUpdatedCategoryDto = CategoryDto.builder()
                .id(UUID.randomUUID())
                .name("Health")
                .postCount(5)
                .build();
    }

    @Test
    @DisplayName("Success Get Categories")
    void listCategories_ShouldReturnListOfCategories() {
        List<CategoryDto> categoryList = List.of(categoryDto);
        when(categoryService.listCategories()).thenReturn(categoryList);

        ResponseEntity<List<CategoryDto>> response = categoryController.listCategories();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(categoryList).hasSize(1);

        verify(categoryService).listCategories();
    }

    @Test
    @DisplayName("Success Get Category")
    void getCategory_ShouldReturnCategory_WhenCategoryExists() {
        UUID categoryId = UUID.randomUUID();
        when(categoryService.getCategoryById(eq(categoryId))).thenReturn(categoryDto);

        ResponseEntity<CategoryDto> response = categoryController.getCategory(categoryId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(categoryDto);

        verify(categoryService).getCategoryById(eq(categoryId));
    }

    @Test
    @DisplayName("Fail to Get Category - Not Found")
    void getCategory_ShouldThrowException_WhenCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryService.getCategoryById(eq(categoryId))).thenThrow(new EntityNotFoundException("Category not found"));

        Exception exception = assertThrows(EntityNotFoundException.class, () -> categoryController.getCategory(categoryId));
        assertThat(exception.getMessage()).isEqualTo("Category not found");

        verify(categoryService).getCategoryById(eq(categoryId));
    }

    @Test
    @DisplayName("Success Create Category")
    void createCategory_ShouldReturnCreatedCategory() {
        when(categoryService.createCategory(eq(createCategoryRequestDto)))
                .thenReturn(expectedCreatedCategoryDto);

        ResponseEntity<CategoryDto> response = categoryController.createCategory(createCategoryRequestDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedCreatedCategoryDto);

        verify(categoryService).createCategory(eq(createCategoryRequestDto));
    }

    @Test
    @DisplayName("Success Update Category - Update Count")
    void updateCategory_ShouldReturnUpdatedCategoryWithNewCount() {
        UUID updateCategoryId = UUID.randomUUID();
        when(categoryService.updateCategory(eq(updateCategoryId), eq(updateCategoryRequestDto)))
                .thenReturn(expectedUpdatedCategoryDto);

        ResponseEntity<CategoryDto> response = categoryController.updateCategory(updateCategoryId, updateCategoryRequestDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedUpdatedCategoryDto);

        verify(categoryService).updateCategory(eq(updateCategoryId), eq(updateCategoryRequestDto));
    }

    @Test
    @DisplayName("Success Delete Category")
    void deleteCategory_ShouldReturnNoContent() {
        UUID categoryId = UUID.randomUUID();
        doNothing().when(categoryService).deleteCategory(eq(categoryId));

        ResponseEntity<Void> response = categoryController.deleteCategory(categoryId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(categoryService, times(1)).deleteCategory(eq(categoryId));
    }
}
