package com.example.eventsystem.mapper;

import com.example.eventsystem.model.dto.CategoryRequestDto;
import com.example.eventsystem.model.dto.CategoryResponseDto;
import com.example.eventsystem.model.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity(CategoryRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public CategoryResponseDto toResponseDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponseDto(category.getId(), category.getName());
    }
}