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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Создание мероприятия.
     * POST /api/v1/events
     */
    @PostMapping
    public ResponseEntity<EventResponseDto> create(@RequestBody EventRequestDto requestDto) {
        EventResponseDto created = eventService.createEvent(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Получение конкретного мероприятия.
     * GET /api/v1/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    /**
     * Получение списка мероприятий по фильтру статуса.
     * GET /api/v1/events?status=PLANNED
     */
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getByStatus(
            @RequestParam(name = "status") EventStatus status) {
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
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