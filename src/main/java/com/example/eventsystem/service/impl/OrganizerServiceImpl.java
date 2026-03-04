package com.example.eventsystem.service.impl;

import com.example.eventsystem.mapper.OrganizerMapper;
import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;
import com.example.eventsystem.model.entity.Organizer;
import com.example.eventsystem.repository.OrganizerRepository;
import com.example.eventsystem.service.OrganizerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizerServiceImpl implements OrganizerService {
    private final OrganizerRepository organizerRepository;
    private final OrganizerMapper organizerMapper;

    @Override
    @Transactional
    public OrganizerResponseDto create(OrganizerRequestDto dto) {
        Organizer organizer = organizerMapper.toEntity(dto);
        return organizerMapper.toResponseDto(organizerRepository.save(organizer));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizerResponseDto getById(Long id) {
        return organizerRepository.findById(id)
                .map(organizerMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));
    }
}