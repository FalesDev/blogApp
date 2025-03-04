package com.falesdev.blog.services.impl;

import com.falesdev.blog.domain.dtos.CategoryDto;
import com.falesdev.blog.domain.dtos.requests.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdateCategoryRequestDto;
import com.falesdev.blog.domain.entities.Category;
import com.falesdev.blog.mappers.CategoryMapper;
import com.falesdev.blog.respositories.CategoryRepository;
import com.falesdev.blog.services.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> listCategories() {
        return categoryRepository.findAllWithPostCount().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CreateCategoryRequestDto createCategoryRequestDto) {
        Category category = categoryMapper.toCreateCategory(createCategoryRequestDto);
        if (categoryRepository.existsByNameIgnoreCase(category.getName())){
            throw new IllegalArgumentException("Category already exists with name: " + category.getName());
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(UUID id, UpdateCategoryRequestDto updateCategoryRequestDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Category does not exist with id "+id));

        categoryMapper.updateFromDto(updateCategoryRequestDto,existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(UUID id) {
        categoryRepository.findById(id).ifPresent(category -> {
            if (!category.getPosts().isEmpty()) {
                throw new IllegalStateException("Category has posts associated with it");
            }
            categoryRepository.deleteById(id);
        });
    }

    @Override
    public CategoryDto getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id" + id));
        return categoryMapper.toDto(category);
    }
}
