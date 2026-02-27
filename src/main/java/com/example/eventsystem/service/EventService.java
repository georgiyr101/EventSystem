package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.EventRequestDto;
import com.example.eventsystem.model.dto.EventResponseDto;
import com.example.eventsystem.model.enums.EventStatus;

import java.util.List;

public interface EventService {

    EventResponseDto createEvent(EventRequestDto requestDto);

    EventResponseDto getEventById(Long id);

    List<EventResponseDto> getEventsByStatus(EventStatus status);

    EventResponseDto updateStatus(Long id, EventStatus newStatus);
}