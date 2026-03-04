package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;

import java.util.List;

public interface OrganizerService {

    OrganizerResponseDto create(OrganizerRequestDto dto);

    OrganizerResponseDto getById(Long id);

    List<OrganizerResponseDto> searchByName(String name);

    OrganizerResponseDto update(Long id, OrganizerRequestDto dto);

    void delete(Long id);

    List<OrganizerResponseDto> getAllOrganizers();
}