package com.example.eventsystem.service.impl;

import com.example.eventsystem.mapper.EventMapper;
import com.example.eventsystem.model.dto.EventRequestDto;
import com.example.eventsystem.model.dto.EventResponseDto;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.enums.EventStatus;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.service.EventService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponseDto createEvent(EventRequestDto requestDto) {
        Event event = eventMapper.toEntity(requestDto);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponseDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getEventById(Long id) {
        return eventRepository.findById(id)
                .map(eventMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsByStatus(EventStatus status) {
        return eventRepository.findAllByStatus(status).stream()
                .map(eventMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public EventResponseDto updateStatus(Long id, EventStatus newStatus) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        event.setStatus(newStatus);
        return eventMapper.toResponseDto(eventRepository.save(event));
    }
}