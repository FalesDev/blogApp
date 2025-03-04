package com.falesdev.blog.controllers;

import com.falesdev.blog.domain.dtos.CategoryDto;
import com.falesdev.blog.domain.dtos.requests.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdateCategoryRequestDto;
import com.falesdev.blog.services.CategoryService;
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
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> listCategories(){
        return ResponseEntity.ok(categoryService.listCategories());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable UUID id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto){
        return new ResponseEntity<>(
                categoryService.createCategory(createCategoryRequestDto),
                HttpStatus.CREATED
        );
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<CategoryDto> updateCategory (
            @PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequestDto updateCategoryRequestDto){
        return ResponseEntity.ok(categoryService.updateCategory(id,updateCategoryRequestDto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id){
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
