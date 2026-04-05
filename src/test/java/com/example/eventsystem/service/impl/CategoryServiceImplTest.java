package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.mapper.CategoryMapper;
import com.example.eventsystem.model.dto.CategoryRequestDto;
import com.example.eventsystem.model.dto.CategoryResponseDto;
import com.example.eventsystem.model.entity.Category;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryResponseDto responseDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Concerts")
                .events(new HashSet<>())
                .build();

        responseDto = new CategoryResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Concerts");
    }

    @Test
    void getById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponseDto(category)).thenReturn(responseDto);

        CategoryResponseDto result = categoryService.getById(1L);

        assertNotNull(result);
        assertEquals("Concerts", result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getById(99L));
    }

    @Test
    void getAll_WithFilter_ReturnsFilteredList() {
        Category cat2 = Category.builder().id(2L).name("Festivals").build();
        when(categoryRepository.findAll()).thenReturn(List.of(category, cat2));
        when(categoryMapper.toResponseDto(category)).thenReturn(responseDto);

        List<CategoryResponseDto> result = categoryService.getAll("con");

        assertEquals(1, result.size());
        verify(categoryMapper, times(1)).toResponseDto(any());
    }

    @Test
    void delete_ShouldUnlinkEventsBeforeRemoving() {
        Event event = new Event();
        event.setCategories(new HashSet<>(List.of(category)));
        category.getEvents().add(event);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        assertTrue(event.getCategories().isEmpty(), "Category should be removed from event");
        verify(categoryRepository).delete(category);
    }

    @Test
    void create_Success() {
        CategoryRequestDto requestDto = new CategoryRequestDto();
        requestDto.setName("New Category");

        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponseDto(category)).thenReturn(responseDto);

        CategoryResponseDto result = categoryService.create(requestDto);

        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
    }
}