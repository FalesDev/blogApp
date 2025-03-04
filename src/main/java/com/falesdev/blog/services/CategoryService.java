package com.falesdev.blog.services;


import com.falesdev.blog.domain.dtos.CategoryDto;
import com.falesdev.blog.domain.dtos.requests.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdateCategoryRequestDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryDto> listCategories();
    CategoryDto createCategory(CreateCategoryRequestDto createCategoryRequestDto);
    CategoryDto updateCategory(UUID id, UpdateCategoryRequestDto updateCategoryRequestDto);
    void deleteCategory(UUID id);
    CategoryDto getCategoryById(UUID id);
}
