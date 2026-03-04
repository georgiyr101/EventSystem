package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.CategoryRequestDto;
import com.example.eventsystem.model.dto.CategoryResponseDto;
import com.example.eventsystem.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Создание новой категории.
     * POST /api/v1/categories
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@RequestBody CategoryRequestDto requestDto) {
        return new ResponseEntity<>(categoryService.create(requestDto), HttpStatus.CREATED);
    }

    /**
     * Получение категории по уникальному идентификатору.
     * GET /api/v1/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * Получение списка всех категорий с опциональной фильтрацией по имени.
     * GET /api/v1/categories?name=Концерт
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAll(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(categoryService.getAll(name));
    }

    /**
     * Полное обновление данных категории.
     * PUT /api/v1/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable Long id,
                                                      @RequestBody CategoryRequestDto requestDto) {
        return ResponseEntity.ok(categoryService.update(id, requestDto));
    }

    /**
     * Удаление категории из системы.
     * DELETE /api/v1/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
