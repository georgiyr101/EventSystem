package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;
import com.example.eventsystem.service.OrganizerService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/organizers")
@RequiredArgsConstructor
@Tag(name = "Organizer Controller", description = "Управление организаторами мероприятий")
public class OrganizerController {

    private final OrganizerService organizerService;

    @Operation(summary = "Регистрация нового организатора",
            description = "Создает профиль организатора. Поля name и contactInfo обязательны.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Организатор успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
    })
    @PostMapping
    public ResponseEntity<OrganizerResponseDto> create(@Valid @RequestBody OrganizerRequestDto requestDto) {
        return new ResponseEntity<>(organizerService.create(requestDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Получить список всех организаторов")
    @GetMapping
    public ResponseEntity<List<OrganizerResponseDto>> getAll() {
        return ResponseEntity.ok(organizerService.getAllOrganizers());
    }

    @Operation(summary = "Найти организатора по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Организатор найден"),
            @ApiResponse(responseCode = "404", description = "Организатор с таким ID не существует")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganizerResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(organizerService.getById(id));
    }

    @Operation(summary = "Поиск по имени",
            description = "Возвращает список организаторов, чье имя содержит указанную строку.")
    @GetMapping("/search")
    public ResponseEntity<List<OrganizerResponseDto>> search(@RequestParam String name) {
        return ResponseEntity.ok(organizerService.searchByName(name));
    }

    @Operation(summary = "Обновить данные организатора")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно обновлены"),
            @ApiResponse(responseCode = "404", description = "Организатор не найден"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrganizerResponseDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody OrganizerRequestDto requestDto) {
        return ResponseEntity.ok(organizerService.update(id, requestDto));
    }

    @Operation(summary = "Удалить организатора")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Организатор удален"),
            @ApiResponse(responseCode = "404", description = "Организатор не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organizerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
