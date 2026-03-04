package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;
import com.example.eventsystem.service.OrganizerService;
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
public class OrganizerController {

    private final OrganizerService organizerService;

    /**
     * Регистрация нового организатора.
     * POST /api/v1/organizers
     */
    @PostMapping
    public ResponseEntity<OrganizerResponseDto> create(@RequestBody OrganizerRequestDto requestDto) {
        return new ResponseEntity<>(organizerService.create(requestDto), HttpStatus.CREATED);
    }

    /**
     * Получение данных организатора по ID.
     * GET /api/v1/organizers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrganizerResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(organizerService.getById(id));
    }

    /**
     * Поиск организаторов по частичному совпадению имени.
     * GET /api/v1/organizers/search?name=EventAgency
     */
    @GetMapping("/search")
    public ResponseEntity<List<OrganizerResponseDto>> search(@RequestParam String name) {
        return ResponseEntity.ok(organizerService.searchByName(name));
    }

    /**
     * Обновление контактной информации организатора.
     * PUT /api/v1/organizers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrganizerResponseDto> update(@PathVariable Long id,
                                                       @RequestBody OrganizerRequestDto requestDto) {
        return ResponseEntity.ok(organizerService.update(id, requestDto));
    }

    /**
     * Удаление организатора.
     * DELETE /api/v1/organizers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organizerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
