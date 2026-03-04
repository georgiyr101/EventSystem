package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.EventRequestDto;
import com.example.eventsystem.model.dto.EventResponseDto;
import com.example.eventsystem.model.enums.EventStatus;
import com.example.eventsystem.service.EventService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Создание нового мероприятия.
     * POST /api/v1/events
     */
    @PostMapping
    public ResponseEntity<EventResponseDto> create(@RequestBody EventRequestDto requestDto) {
        return new ResponseEntity<>(eventService.createEvent(requestDto), HttpStatus.CREATED);
    }

    /**
     * Получение списка всех мероприятий.
     * GET /api/v1/events
     */
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAll() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * Получение подробной информации о мероприятии.
     * GET /api/v1/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    /**
     * Фильтрация мероприятий по статусу (PLANNED, ONGOING, etc).
     * GET /api/v1/events/filter?status=PLANNED
     */
    @GetMapping("/filter")
    public ResponseEntity<List<EventResponseDto>> filterByStatus(@RequestParam EventStatus status) {
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
    }

    /**
     * Обновление параметров существующего мероприятия.
     * PUT /api/v1/events/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> update(@PathVariable Long id, @RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(eventService.updateEvent(id, requestDto));
    }

    /**
     * Удаление мероприятия.
     * DELETE /api/v1/events/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Изменение статуса мероприятия.
     * PATCH /api/v1/events/{id}/status?newStatus=CANCELLED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<EventResponseDto> changeStatus(
            @PathVariable Long id,
            @RequestParam EventStatus newStatus) {
        return ResponseEntity.ok(eventService.updateStatus(id, newStatus));
    }
}