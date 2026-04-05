package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.mapper.OrganizerMapper;
import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;
import com.example.eventsystem.model.entity.Organizer;
import com.example.eventsystem.repository.OrganizerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerServiceImplTest {

    @Mock
    private OrganizerRepository organizerRepository;
    @Mock
    private OrganizerMapper organizerMapper;

    @InjectMocks
    private OrganizerServiceImpl organizerService;

    @Test
    void create_shouldSaveOrganizer() {
        OrganizerRequestDto dto = new OrganizerRequestDto("Tech", "tech@example.com");
        Organizer organizer = Organizer.builder().name("Tech").contactInfo("tech@example.com").build();
        Organizer saved = Organizer.builder().id(1L).name("Tech").contactInfo("tech@example.com").build();
        OrganizerResponseDto response = new OrganizerResponseDto(1L, "Tech", "tech@example.com");

        when(organizerMapper.toEntity(dto)).thenReturn(organizer);
        when(organizerRepository.save(organizer)).thenReturn(saved);
        when(organizerMapper.toResponseDto(saved)).thenReturn(response);

        OrganizerResponseDto actual = organizerService.create(dto);

        assertEquals(1L, actual.getId());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(organizerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> organizerService.getById(2L));
    }

    @Test
    void searchByName_shouldFilterIgnoringCase() {
        Organizer o1 = Organizer.builder().id(1L).name("Alpha Org").contactInfo("a@a").build();
        Organizer o2 = Organizer.builder().id(2L).name("Beta Team").contactInfo("b@b").build();

        when(organizerRepository.findAll()).thenReturn(List.of(o1, o2));
        when(organizerMapper.toResponseDto(o2)).thenReturn(new OrganizerResponseDto(2L, "Beta Team", "b@b"));

        List<OrganizerResponseDto> actual = organizerService.searchByName("BETA");

        assertEquals(1, actual.size());
        assertEquals("Beta Team", actual.getFirst().getName());
    }

    @Test
    void update_shouldApplyFields() {
        OrganizerRequestDto dto = new OrganizerRequestDto("New Name", "new@example.com");
        Organizer organizer = Organizer.builder().id(5L).name("Old").contactInfo("old@example.com").build();
        OrganizerResponseDto response = new OrganizerResponseDto(5L, "New Name", "new@example.com");

        when(organizerRepository.findById(5L)).thenReturn(Optional.of(organizer));
        when(organizerRepository.save(organizer)).thenReturn(organizer);
        when(organizerMapper.toResponseDto(organizer)).thenReturn(response);

        OrganizerResponseDto actual = organizerService.update(5L, dto);

        assertEquals("New Name", organizer.getName());
        assertEquals("new@example.com", actual.getContactInfo());
    }

    @Test
    void delete_shouldThrowWhenNotExists() {
        when(organizerRepository.existsById(7L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> organizerService.delete(7L));
    }

    @Test
    void delete_shouldDeleteById() {
        when(organizerRepository.existsById(7L)).thenReturn(true);

        organizerService.delete(7L);

        verify(organizerRepository).deleteById(7L);
    }

    @Test
    void getAllOrganizers_shouldMapAll() {
        Organizer organizer = Organizer.builder().id(3L).name("Org").contactInfo("c").build();
        when(organizerRepository.findAll()).thenReturn(List.of(organizer));
        when(organizerMapper.toResponseDto(organizer)).thenReturn(new OrganizerResponseDto(3L, "Org", "c"));

        List<OrganizerResponseDto> actual = organizerService.getAllOrganizers();

        assertEquals(1, actual.size());
    }
}
