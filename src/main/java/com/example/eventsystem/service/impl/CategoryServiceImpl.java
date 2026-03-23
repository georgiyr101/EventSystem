package com.example.eventsystem.service.impl;

import com.example.eventsystem.mapper.CategoryMapper;
import com.example.eventsystem.model.dto.CategoryRequestDto;
import com.example.eventsystem.model.dto.CategoryResponseDto;
import com.example.eventsystem.model.entity.Category;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.repository.CategoryRepository;
import com.example.eventsystem.service.CategoryService;
import com.example.eventsystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponseDto create(CategoryRequestDto dto) {
        Category category = categoryMapper.toEntity(dto);
        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto getById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toResponseDto).orElseThrow();
    }

    @Override
    @Transactional
    public CategoryResponseDto update(Long id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found"));

        category.setName(dto.getName());
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAll(String name) {
        return categoryRepository.findAll().stream()
                .filter(category -> name == null
                        || category.getName().toLowerCase().contains(name.toLowerCase()))
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        for (Event event : category.getEvents()) {
            event.getCategories().remove(category);
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }
}