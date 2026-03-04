package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;

public interface OrganizerService {

    OrganizerResponseDto create(OrganizerRequestDto dto);

    OrganizerResponseDto getById(Long id);
}