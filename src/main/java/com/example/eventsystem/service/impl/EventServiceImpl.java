package com.example.eventsystem.service.impl;

import com.example.eventsystem.mapper.EventMapper;
import com.example.eventsystem.model.dto.EventRequestDto;
import com.example.eventsystem.model.dto.EventResponseDto;
import com.example.eventsystem.model.entity.Category;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.entity.Organizer;
import com.example.eventsystem.model.enums.EventStatus;
import com.example.eventsystem.repository.CategoryRepository;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.repository.OrganizerRepository;
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
    private final OrganizerRepository organizerRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public EventResponseDto createEvent(EventRequestDto requestDto) {
        Organizer organizer = organizerRepository.findById(requestDto.getOrganizerId())
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));

        Event event = eventMapper.toEntity(requestDto);

        event.setOrganizer(organizer);
        event.setStatus(EventStatus.PLANNED);

        if (requestDto.getCategoryIds() != null && !requestDto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(requestDto.getCategoryIds());
            event.setCategories(new java.util.HashSet<>(categories));
        }

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

    @Override
    @Transactional
    public EventResponseDto updateEvent(Long id, EventRequestDto requestDto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

        // 1. Обновляем простые поля
        event.setName(requestDto.getName());
        event.setStartDate(requestDto.getStartDate());
        event.setEndDate(requestDto.getEndDate());
        event.setMaxParticipants(requestDto.getMaxParticipants());
        event.setTicketPrice(requestDto.getTicketPrice());

        // 2. ОБНОВЛЯЕМ ОРГАНИЗАТОРА (важно, так как в PUT ты прислал id: 2)
        if (requestDto.getOrganizerId() != null) {
            Organizer organizer = organizerRepository.findById(requestDto.getOrganizerId())
                    .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));
            event.setOrganizer(organizer);
        }

        // 3. ОБНОВЛЯЕМ КАТЕГОРИИ (вот чего не хватало!)
        if (requestDto.getCategoryIds() != null) {
            // Достаем из БД полные объекты категорий (чтобы в ответе не было null)
            List<Category> categories = categoryRepository.findAllById(requestDto.getCategoryIds());
            event.setCategories(new java.util.HashSet<>(categories));
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toResponseDto(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Cannot delete: Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponseDto)
                .toList();
    }
}