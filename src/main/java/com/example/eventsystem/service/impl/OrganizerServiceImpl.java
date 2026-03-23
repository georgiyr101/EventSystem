package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.mapper.OrganizerMapper;
import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;
import com.example.eventsystem.model.entity.Organizer;
import com.example.eventsystem.repository.OrganizerRepository;
import com.example.eventsystem.service.OrganizerService;
import com.example.eventsystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerResponseDto> searchByName(String name) {
        return organizerRepository.findAll().stream()
                .filter(o -> name == null || o.getName().toLowerCase().contains(name.toLowerCase()))
                .map(organizerMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public OrganizerResponseDto update(Long id, OrganizerRequestDto dto) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        organizer.setName(dto.getName());
        organizer.setContactInfo(dto.getContactInfo());

        return organizerMapper.toResponseDto(organizerRepository.save(organizer));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!organizerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Organizer not found");
        }
        organizerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerResponseDto> getAllOrganizers() {
        return organizerRepository.findAll().stream()
                .map(organizerMapper::toResponseDto)
                .toList();
    }
}