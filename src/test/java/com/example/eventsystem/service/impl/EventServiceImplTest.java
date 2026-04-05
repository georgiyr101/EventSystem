package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ConflictException;
import com.example.eventsystem.exception.ValidationException;
import com.example.eventsystem.mapper.EventMapper;
import com.example.eventsystem.model.dto.EventRequestDto;
import com.example.eventsystem.model.dto.EventResponseDto;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.entity.Organizer;
import com.example.eventsystem.model.enums.EventStatus;
import com.example.eventsystem.repository.CategoryRepository;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.repository.OrganizerRepository;
import com.example.eventsystem.service.EventSearchCacheIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock private EventRepository eventRepository;
    @Mock private EventMapper eventMapper;
    @Mock private OrganizerRepository organizerRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private EventSearchCacheIndex cacheIndex;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventRequestDto requestDto;
    private Event event;

    @BeforeEach
    void setUp() {
        requestDto = new EventRequestDto();
        requestDto.setName("Java Tech Talk");
        requestDto.setStartDate(LocalDateTime.now().plusDays(1));
        requestDto.setEndDate(LocalDateTime.now().plusDays(2));
        requestDto.setOrganizerId(1L);

        event = new Event();
        event.setId(100L);
        event.setStatus(EventStatus.PLANNED);
    }

    @Test
    void createEvent_ShouldThrowException_WhenDatesAreInvalid() {
        requestDto.setEndDate(requestDto.getStartDate().minusHours(1));

        assertThrows(ValidationException.class, () -> eventService.createEvent(requestDto));
        verifyNoInteractions(eventRepository);
    }

    @Test
    void createEvent_Success() {
        Organizer organizer = new Organizer();
        when(organizerRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(eventMapper.toEntity(requestDto)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponseDto(event)).thenReturn(new EventResponseDto());

        eventService.createEvent(requestDto);

        verify(eventRepository).save(event);
        verify(cacheIndex).clear();
    }

    @Test
    void deleteEvent_ShouldThrowConflict_WhenEventIsCompleted() {
        event.setStatus(EventStatus.COMPLETED);
        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () -> eventService.deleteEvent(100L));
        verify(eventRepository, never()).delete(any());
    }

    @Test
    void searchEvents_ShouldReturnFromCache_WhenCacheHit() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventResponseDto> cachedPage = new PageImpl<>(List.of(new EventResponseDto()));

        when(cacheIndex.get(any())).thenReturn(cachedPage);

        Page<EventResponseDto> result = eventService.searchEvents("Rock", 100.0, "Club", pageable, false);

        assertEquals(cachedPage, result);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void searchEvents_ShouldQueryRepo_WhenCacheMiss() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Long> idPage = new PageImpl<>(List.of(100L));

        when(cacheIndex.get(any())).thenReturn(null); // Промах кэша
        when(eventRepository.findIdsByFilterJpql(any(), any(), any(), any())).thenReturn(idPage);
        when(eventRepository.findAllByIdsWithDependencies(any())).thenReturn(List.of(event));
        when(eventMapper.toResponseDto(event)).thenReturn(new EventResponseDto());

        eventService.searchEvents("Tech", null, null, pageable, false);

        verify(eventRepository).findIdsByFilterJpql(any(), any(), any(), any());
        verify(cacheIndex).put(any(), any());
    }
}