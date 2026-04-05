package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.mapper.CategoryMapper;
import com.example.eventsystem.model.dto.CategoryRequestDto;
import com.example.eventsystem.model.dto.CategoryResponseDto;
import com.example.eventsystem.model.entity.Category;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void create_shouldSaveCategory() {
        CategoryRequestDto dto = new CategoryRequestDto("Music");
        Category category = Category.builder().name("Music").build();
        Category saved = Category.builder().id(1L).name("Music").build();
        CategoryResponseDto response = new CategoryResponseDto(1L, "Music");

        when(categoryMapper.toEntity(dto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(saved);
        when(categoryMapper.toResponseDto(saved)).thenReturn(response);

        CategoryResponseDto actual = categoryService.create(dto);

        assertEquals(1L, actual.getId());
        assertEquals("Music", actual.getName());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(categoryRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getById(7L));
    }

    @Test
    void update_shouldUpdateName() {
        CategoryRequestDto dto = new CategoryRequestDto("Tech");
        Category category = Category.builder().id(3L).name("Old").build();
        CategoryResponseDto response = new CategoryResponseDto(3L, "Tech");

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponseDto(category)).thenReturn(response);

        CategoryResponseDto actual = categoryService.update(3L, dto);

        assertEquals("Tech", category.getName());
        assertEquals("Tech", actual.getName());
    }

    @Test
    void getAll_shouldFilterByName() {
        Category c1 = Category.builder().id(1L).name("Music").build();
        Category c2 = Category.builder().id(2L).name("Technology").build();

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));
        when(categoryMapper.toResponseDto(c2)).thenReturn(new CategoryResponseDto(2L, "Technology"));

        List<CategoryResponseDto> actual = categoryService.getAll("tech");

        assertEquals(1, actual.size());
        assertEquals("Technology", actual.getFirst().getName());
    }

    @Test
    void delete_shouldDetachFromEventsAndDelete() {
        Category category = Category.builder().id(9L).name("Business").build();
        Event event = Event.builder().id(10L).build();
        Set<Category> categories = new HashSet<>();
        categories.add(category);
        event.setCategories(categories);
        category.setEvents(Set.of(event));

        when(categoryRepository.findById(9L)).thenReturn(Optional.of(category));

        categoryService.delete(9L);

        assertEquals(0, event.getCategories().size());
        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(1L));
    }

    @Test
    void getAllCategories_shouldReturnMappedList() {
        Category c1 = Category.builder().id(1L).name("Music").build();

        when(categoryRepository.findAll()).thenReturn(List.of(c1));
        when(categoryMapper.toResponseDto(any(Category.class))).thenReturn(new CategoryResponseDto(1L, "Music"));

        List<CategoryResponseDto> actual = categoryService.getAllCategories();

        assertEquals(1, actual.size());
    }
}
