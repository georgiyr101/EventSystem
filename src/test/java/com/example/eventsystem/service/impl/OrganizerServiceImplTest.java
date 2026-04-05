package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.mapper.OrganizerMapper;
import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;
import com.example.eventsystem.model.entity.Organizer;
import com.example.eventsystem.repository.OrganizerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizerServiceImplTest {

    @Mock
    private OrganizerRepository organizerRepository;

    @Mock
    private OrganizerMapper organizerMapper;

    @InjectMocks
    private OrganizerServiceImpl organizerService;

    private Organizer organizer;
    private OrganizerResponseDto responseDto;

    @BeforeEach
    void setUp() {
        organizer = new Organizer();
        organizer.setId(1L);
        organizer.setName("Global Events");
        organizer.setContactInfo("info@global.com");

        responseDto = new OrganizerResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Global Events");
        responseDto.setContactInfo("info@global.com");
    }

    @Test
    void create_Success() {
        OrganizerRequestDto requestDto = new OrganizerRequestDto();
        requestDto.setName("New Org");

        when(organizerMapper.toEntity(any(OrganizerRequestDto.class))).thenReturn(organizer);
        when(organizerRepository.save(any(Organizer.class))).thenReturn(organizer);
        when(organizerMapper.toResponseDto(organizer)).thenReturn(responseDto);

        OrganizerResponseDto result = organizerService.create(requestDto);

        assertNotNull(result);
        assertEquals("Global Events", result.getName());
        verify(organizerRepository).save(any(Organizer.class));
    }

    @Test
    void getById_Success() {
        when(organizerRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(organizerMapper.toResponseDto(organizer)).thenReturn(responseDto);

        OrganizerResponseDto result = organizerService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(organizerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> organizerService.getById(99L));
    }

    @Test
    void searchByName_ShouldFilterCorrectly() {
        Organizer other = new Organizer();
        other.setName("Local Music");

        when(organizerRepository.findAll()).thenReturn(List.of(organizer, other));
        when(organizerMapper.toResponseDto(organizer)).thenReturn(responseDto);

        List<OrganizerResponseDto> result = organizerService.searchByName("global");

        assertEquals(1, result.size());
        assertEquals("Global Events", result.getFirst().getName());
    }

    @Test
    void update_Success() {
        OrganizerRequestDto updateDto = new OrganizerRequestDto();
        updateDto.setName("Updated Name");
        updateDto.setContactInfo("new@contact.com");

        when(organizerRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(organizerRepository.save(any(Organizer.class))).thenReturn(organizer);
        when(organizerMapper.toResponseDto(any(Organizer.class))).thenReturn(responseDto);

        organizerService.update(1L, updateDto);

        assertEquals("Updated Name", organizer.getName());
        verify(organizerRepository).save(organizer);
    }

    @Test
    void delete_Success() {
        when(organizerRepository.existsById(1L)).thenReturn(true);

        organizerService.delete(1L);

        verify(organizerRepository).deleteById(1L);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(organizerRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> organizerService.delete(1L));
        verify(organizerRepository, never()).deleteById(anyLong());
    }
}