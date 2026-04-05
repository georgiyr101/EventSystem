package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ConflictException;
import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.exception.ValidationException;
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
import com.example.eventsystem.service.EventSearchCacheIndex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private OrganizerRepository organizerRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventSearchCacheIndex cacheIndex;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void createEvent_shouldThrowWhenEndDateBeforeStartDate() {
        EventRequestDto request = requestDto();
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () -> eventService.createEvent(request));
    }

    @Test
    void createEvent_shouldThrowWhenOrganizerNotFound() {
        EventRequestDto request = requestDto();

        when(organizerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(request));
    }

    @Test
    void createEvent_shouldSetPlannedStatusAndCategoriesAndClearCache() {
        EventRequestDto request = requestDto();
        Organizer organizer = Organizer.builder().id(5L).name("Org").build();
        Event mapped = Event.builder().name("Meetup").build();
        Event saved = Event.builder().id(10L).name("Meetup").status(EventStatus.PLANNED).build();
        EventResponseDto response = EventResponseDto.builder().id(10L).name("Meetup").build();
        Category category = Category.builder().id(7L).name("Tech").build();

        when(organizerRepository.findById(5L)).thenReturn(Optional.of(organizer));
        when(eventMapper.toEntity(request)).thenReturn(mapped);
        when(categoryRepository.findAllById(List.of(7L))).thenReturn(List.of(category));
        when(eventRepository.save(mapped)).thenReturn(saved);
        when(eventMapper.toResponseDto(saved)).thenReturn(response);

        EventResponseDto actual = eventService.createEvent(request);

        assertEquals(10L, actual.getId());
        assertEquals(EventStatus.PLANNED, mapped.getStatus());
        assertEquals(1, mapped.getCategories().size());
        verify(cacheIndex).clear();
    }

    @Test
    void createEvent_shouldWorkWhenCategoryIdsAreNull() {
        EventRequestDto request = requestDto();
        request.setCategoryIds(null);
        Organizer organizer = Organizer.builder().id(5L).name("Org").build();
        Event mapped = Event.builder().name("Meetup").build();
        Event saved = Event.builder().id(10L).name("Meetup").status(EventStatus.PLANNED).build();
        EventResponseDto response = EventResponseDto.builder().id(10L).name("Meetup").build();

        when(organizerRepository.findById(5L)).thenReturn(Optional.of(organizer));
        when(eventMapper.toEntity(request)).thenReturn(mapped);
        when(eventRepository.save(mapped)).thenReturn(saved);
        when(eventMapper.toResponseDto(saved)).thenReturn(response);

        EventResponseDto actual = eventService.createEvent(request);

        assertEquals(10L, actual.getId());
        verify(categoryRepository, never()).findAllById(any());
    }

    @Test
    void getEventById_shouldThrowWhenMissing() {
        when(eventRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(3L));
    }

    @Test
    void updateStatus_shouldSaveNewStatus() {
        Event event = Event.builder().id(3L).status(EventStatus.PLANNED).build();
        EventResponseDto response = EventResponseDto.builder().id(3L).statusCode("COMPLETED").build();

        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponseDto(event)).thenReturn(response);

        EventResponseDto actual = eventService.updateStatus(3L, EventStatus.COMPLETED);

        assertEquals(EventStatus.COMPLETED, event.getStatus());
        assertEquals("COMPLETED", actual.getStatusCode());
    }

    @Test
    void updateStatus_shouldThrowWhenEventMissing() {
        when(eventRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.updateStatus(3L, EventStatus.CANCELLED));
    }

    @Test
    void updateEvent_shouldUpdateFieldsOrganizerCategoriesAndClearCache() {
        EventRequestDto request = requestDto();
        request.setName("Updated");
        request.setCategoryIds(List.of(1L, 2L));
        Organizer organizer = Organizer.builder().id(5L).name("Org").build();
        Event event = Event.builder().id(8L).name("Old").status(EventStatus.PLANNED).build();
        EventResponseDto response = EventResponseDto.builder().id(8L).name("Updated").build();

        when(eventRepository.findById(8L)).thenReturn(Optional.of(event));
        when(organizerRepository.findById(5L)).thenReturn(Optional.of(organizer));
        when(categoryRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                Category.builder().id(1L).name("Tech").build(),
                Category.builder().id(2L).name("Business").build()
        ));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponseDto(event)).thenReturn(response);

        EventResponseDto actual = eventService.updateEvent(8L, request);

        assertEquals("Updated", event.getName());
        assertEquals(2, event.getCategories().size());
        assertEquals("Updated", actual.getName());
        verify(cacheIndex).clear();
    }

    @Test
    void updateEvent_shouldSkipOrganizerAndCategoriesWhenNull() {
        EventRequestDto request = requestDto();
        request.setOrganizerId(null);
        request.setCategoryIds(null);
        Event event = Event.builder().id(8L).name("Old").status(EventStatus.PLANNED).build();
        EventResponseDto response = EventResponseDto.builder().id(8L).name("Meetup").build();

        when(eventRepository.findById(8L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponseDto(event)).thenReturn(response);

        EventResponseDto actual = eventService.updateEvent(8L, request);

        assertEquals(8L, actual.getId());
        verify(organizerRepository, never()).findById(any());
        verify(categoryRepository, never()).findAllById(any());
    }

    @Test
    void updateEvent_shouldThrowWhenOrganizerMissing() {
        EventRequestDto request = requestDto();
        Event event = Event.builder().id(8L).name("Old").status(EventStatus.PLANNED).build();

        when(eventRepository.findById(8L)).thenReturn(Optional.of(event));
        when(organizerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(8L, request));
    }

    @Test
    void deleteEvent_shouldThrowWhenCompleted() {
        Event event = Event.builder().id(4L).status(EventStatus.COMPLETED).build();
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () -> eventService.deleteEvent(4L));
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void deleteEvent_shouldDeleteAndClearCache() {
        Event event = Event.builder().id(4L).status(EventStatus.PLANNED).build();
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(4L);

        verify(cacheIndex).clear();
        verify(eventRepository).delete(event);
    }

    @Test
    void deleteEvent_shouldThrowWhenEventMissing() {
        when(eventRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.deleteEvent(404L));
    }

    @Test
    void getEventsByStatus_shouldMapAll() {
        Event event = Event.builder().id(11L).status(EventStatus.PLANNED).build();
        EventResponseDto dto = EventResponseDto.builder().id(11L).build();

        when(eventRepository.findAllByStatus(EventStatus.PLANNED)).thenReturn(List.of(event));
        when(eventMapper.toResponseDto(event)).thenReturn(dto);

        List<EventResponseDto> actual = eventService.getEventsByStatus(EventStatus.PLANNED);

        assertEquals(1, actual.size());
        assertEquals(11L, actual.getFirst().getId());
    }

    @Test
    void getAllEvents_shouldMapAll() {
        Event event = Event.builder().id(12L).status(EventStatus.PLANNED).build();
        EventResponseDto dto = EventResponseDto.builder().id(12L).build();

        when(eventRepository.findAll()).thenReturn(List.of(event));
        when(eventMapper.toResponseDto(event)).thenReturn(dto);

        List<EventResponseDto> actual = eventService.getAllEvents();

        assertEquals(1, actual.size());
        assertEquals(12L, actual.getFirst().getId());
    }

    @Test
    void searchEvents_shouldReturnCachedValueOnHit() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventResponseDto> cached = new PageImpl<>(List.of(EventResponseDto.builder().id(1L).build()));

        when(cacheIndex.get(any(EventSearchCacheIndex.EventSearchCacheKey.class))).thenReturn(cached);

        Page<EventResponseDto> actual = eventService.searchEvents("Tech", 10.0, "Org", pageable, false);

        assertEquals(1, actual.getTotalElements());
        verifyNoInteractions(eventRepository);
    }

    @Test
    void searchEvents_shouldReturnEmptyPageWhenNoIds() {
        Pageable pageable = PageRequest.of(0, 10);

        when(cacheIndex.get(any(EventSearchCacheIndex.EventSearchCacheKey.class))).thenReturn(null);
        when(eventRepository.findIdsByFilterJpql("Tech", 10.0, "Org", pageable)).thenReturn(Page.empty(pageable));

        Page<EventResponseDto> actual = eventService.searchEvents("Tech", 10.0, "Org", pageable, false);

        assertTrue(actual.isEmpty());
        verify(cacheIndex, never()).put(any(), any());
    }

    @Test
    void searchEvents_shouldLoadByNativeMapAndPutToCache() {
        Pageable pageable = PageRequest.of(0, 10);
        Event event = Event.builder().id(11L).name("Conf").status(EventStatus.PLANNED)
                .organizer(Organizer.builder().name("Org").build())
                .categories(Set.of(Category.builder().name("Tech").build()))
                .build();
        EventResponseDto dto = EventResponseDto.builder().id(11L).name("Conf").build();
        Page<Long> idPage = new PageImpl<>(List.of(11L), pageable, 1);

        when(cacheIndex.get(any(EventSearchCacheIndex.EventSearchCacheKey.class))).thenReturn(null);
        when(eventRepository.findIdsByFilterNative("Tech", 10.0, "Org", pageable)).thenReturn(idPage);
        when(eventRepository.findAllByIdsWithDependencies(List.of(11L))).thenReturn(List.of(event));
        when(eventMapper.toResponseDto(event)).thenReturn(dto);

        Page<EventResponseDto> actual = eventService.searchEvents("Tech", 10.0, "Org", pageable, true);

        assertEquals(1, actual.getTotalElements());
        assertEquals(11L, actual.getContent().getFirst().getId());
        verify(cacheIndex).put(any(EventSearchCacheIndex.EventSearchCacheKey.class),
                ArgumentMatchers.any(Page.class));
    }

    private static EventRequestDto requestDto() {
        EventRequestDto dto = new EventRequestDto();
        dto.setName("Meetup");
        dto.setStartDate(LocalDateTime.now().plusDays(2));
        dto.setEndDate(LocalDateTime.now().plusDays(3));
        dto.setMaxParticipants(100);
        dto.setTicketPrice(20.0);
        dto.setOrganizerId(5L);
        dto.setCategoryIds(List.of(7L));
        return dto;
    }
}
