package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.CategoryRequestDto;
import com.example.eventsystem.model.dto.CategoryResponseDto;
import java.util.List;

public interface CategoryService {

    CategoryResponseDto create(CategoryRequestDto dto);

    List<CategoryResponseDto> getAll();
}