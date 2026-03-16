package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.EventRequestDto;
import com.example.eventsystem.model.dto.EventResponseDto;
import com.example.eventsystem.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {

    EventResponseDto createEvent(EventRequestDto requestDto);

    EventResponseDto getEventById(Long id);

    List<EventResponseDto> getEventsByStatus(EventStatus status);

    EventResponseDto updateStatus(Long id, EventStatus newStatus);

    EventResponseDto updateEvent(Long id, EventRequestDto requestDto);

    void deleteEvent(Long id);

    List<EventResponseDto> getAllEvents();

    Page<EventResponseDto> searchEvents(String cat, Double price, String org, Pageable pageable,
                                               boolean useNative);

}