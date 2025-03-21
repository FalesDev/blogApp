package com.falesdev.blog.service;


import com.falesdev.blog.domain.dto.CategoryDto;
import com.falesdev.blog.domain.dto.request.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateCategoryRequestDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryDto> listCategories();
    CategoryDto getCategoryById(UUID id);
    CategoryDto createCategory(CreateCategoryRequestDto createCategoryRequestDto);
    CategoryDto updateCategory(UUID id, UpdateCategoryRequestDto updateCategoryRequestDto);
    void deleteCategory(UUID id);
}
