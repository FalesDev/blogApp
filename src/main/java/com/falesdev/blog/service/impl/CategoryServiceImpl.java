package com.falesdev.blog.service.impl;

import com.falesdev.blog.domain.dto.CategoryDto;
import com.falesdev.blog.domain.dto.request.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateCategoryRequestDto;
import com.falesdev.blog.domain.entity.Category;
import com.falesdev.blog.mapper.CategoryMapper;
import com.falesdev.blog.repository.CategoryRepository;
import com.falesdev.blog.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
    public List<CategoryDto> listCategories() {
        return categoryRepository.findAllWithPostCount().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id" + id));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CreateCategoryRequestDto createCategoryRequestDto) {
        if (categoryRepository.existsByNameIgnoreCase(createCategoryRequestDto.getName())){
            throw new IllegalArgumentException("Category already exists with name: "
                    + createCategoryRequestDto.getName());
        }

        Category category = categoryMapper.toCreateCategory(createCategoryRequestDto);
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
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        if (!category.getPosts().isEmpty()) {
            throw new IllegalStateException("Category has posts associated with it");
        }

        categoryRepository.deleteById(id);
    }
}
