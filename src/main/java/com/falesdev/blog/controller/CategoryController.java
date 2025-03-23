package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.CategoryDto;
import com.falesdev.blog.domain.dto.request.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateCategoryRequestDto;
import com.falesdev.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Controller for Categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = "Returns a list of all categories"
    )
    @GetMapping
    public ResponseEntity<List<CategoryDto>> listCategories(){
        return ResponseEntity.ok(categoryService.listCategories());
    }

    @Operation(
            summary = "Get category by ID",
            description = "Returns a single category by its identifier"
    )
    @GetMapping(path = "/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable UUID id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(
            summary = "Create new category",
            description = "Creates a new category and returns the created entity"
    )
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto){
        return new ResponseEntity<>(
                categoryService.createCategory(createCategoryRequestDto),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Update existing category",
            description = "Updates an existing category by its ID"
    )
    @PutMapping(path = "/{id}")
    public ResponseEntity<CategoryDto> updateCategory (
            @PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequestDto updateCategoryRequestDto){
        return ResponseEntity.ok(categoryService.updateCategory(id,updateCategoryRequestDto));
    }

    @Operation(
            summary = "Delete category by ID",
            description = "Delete a category by its identifier"
    )
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id){
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
